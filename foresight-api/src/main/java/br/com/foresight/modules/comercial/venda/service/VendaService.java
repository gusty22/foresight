package br.com.foresight.modules.comercial.venda.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.catalogo.entity.Produto;
import br.com.foresight.modules.comercial.catalogo.repository.IProdutoRepository;
import br.com.foresight.modules.comercial.cliente.entity.Cliente;
import br.com.foresight.modules.comercial.cliente.repository.IClienteRepository;
import br.com.foresight.modules.comercial.venda.dto.ItemVendaDto;
import br.com.foresight.modules.comercial.venda.dto.VendaDto;
import br.com.foresight.modules.comercial.venda.dto.VendaRequest;
import br.com.foresight.modules.comercial.venda.entity.ItemVenda;
import br.com.foresight.modules.comercial.venda.entity.Venda;
import br.com.foresight.modules.comercial.venda.repository.IVendaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import br.com.foresight.modules.financeiro.fluxo_caixa.service.FluxoCaixaService;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final IVendaRepository vendaRepository;
    private final IEmpresaRepository empresaRepository;
    private final IProdutoRepository produtoRepository;
    private final IClienteRepository clienteRepository;
    private final FluxoCaixaService fluxoCaixaService;

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);

    private Empresa getEmpresaLogada() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RegraNegocioException("Acesso Negado: Sessão de tenant inexistente.");
        return empresaRepository.findById(tenantId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada no contexto de segurança."));
    }

    // CREATE
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VendaDto realizarVenda(VendaRequest request) {
        Empresa empresa = getEmpresaLogada();
        Cliente clienteBase = processarCliente(request, empresa);

        LocalDate dataPrev = (request.dataPrevisao() != null && !request.dataPrevisao().isBlank())
                ? LocalDate.parse(request.dataPrevisao()) : null;

        Venda venda = Venda.builder()
                .cliente(request.cliente().nome())
                .clienteId(clienteBase != null ? clienteBase.getId() : null)
                .documentoCliente(request.cliente().documento())
                .telefoneCliente(request.cliente().telefone())
                .formaPagamento(request.formaPagamento())
                .statusPagamento(request.status())
                .dataPrevisaoPagamento(dataPrev)
                .data(LocalDateTime.now())
                .build();
        venda.setEmpresa(empresa);

        List<ItemVenda> itens = processarItensEEstoque(request, empresa, venda);
        venda.setItens(itens);

        // --- CÁLCULO FINANCEIRO SEGURO (BRUTO -> DESCONTO -> LÍQUIDO) ---
        BigDecimal valorBruto = calcularTotal(itens);
        BigDecimal percentualDesc = request.percentualDesconto() != null ? request.percentualDesconto() : BigDecimal.ZERO;

        BigDecimal valorDesconto = valorBruto.multiply(percentualDesc)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal valorLiquidoTotal = valorBruto.subtract(valorDesconto);

        venda.setValorBruto(valorBruto);
        venda.setPercentualDesconto(percentualDesc);
        venda.setValorDesconto(valorDesconto);
        venda.setValorTotal(valorLiquidoTotal);

        vendaRepository.save(venda);

        if ("PAGO".equalsIgnoreCase(request.status())) {
            registrarCaixa(empresa, venda, TipoMovimentacao.ENTRADA);
        }

        return converterParaDto(venda);
    }

    // READ (List)
    @Transactional(readOnly = true)
    public List<VendaDto> listarHistorico() {
        Long tenantId = TenantContext.getCurrentTenant();
        return vendaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .map(this::converterParaDto).toList();
    }

    // READ (Detail)
    @Transactional(readOnly = true)
    public VendaDto buscarDetalhesVenda(Long vendaId) {
        Long tenantId = TenantContext.getCurrentTenant();
        Venda venda = vendaRepository.findByIdAndEmpresaIdWithItens(vendaId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Venda não encontrada ou acesso negado."));
        return converterParaDto(venda);
    }

    // UPDATE: CONFIRMAR PAGAMENTO
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VendaDto confirmarPagamento(Long vendaId) {
        Long tenantId = TenantContext.getCurrentTenant();

        Venda venda = vendaRepository.findByIdAndEmpresaIdForUpdate(vendaId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Venda não encontrada ou acesso negado (Anti-IDOR)."));

        if ("PAGO".equalsIgnoreCase(venda.getStatusPagamento())) {
            throw new RegraNegocioException("Operação negada: Esta venda já consta como paga.");
        }

        venda.setStatusPagamento("PAGO");
        venda.setDataPrevisaoPagamento(LocalDate.now());
        vendaRepository.save(venda);

        registrarCaixa(venda.getEmpresa(), venda, TipoMovimentacao.ENTRADA);

        return converterParaDto(venda);
    }

    // DELETE / ESTORNO
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void excluirOuEstornarVenda(Long vendaId) {
        Long tenantId = TenantContext.getCurrentTenant();
        Venda venda = vendaRepository.findByIdAndEmpresaIdWithItens(vendaId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Venda não encontrada ou acesso negado."));

        if ("PAGO".equalsIgnoreCase(venda.getStatusPagamento())) {
            fluxoCaixaService.registrarMovimentacaoInterna(
                    venda.getEmpresa(),
                    "Estorno/Cancelamento de Venda #" + venda.getId(),
                    venda.getValorTotal(),
                    TipoMovimentacao.SAIDA,
                    CategoriaFluxo.EMPRESA
            );
        }

        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoqueAtual(produto.getEstoqueAtual() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        vendaRepository.delete(venda);
    }

    // PDF GENERATOR
    @Transactional(readOnly = true)
    public void gerarPdfVenda(Long vendaId, HttpServletResponse response) throws IOException {
        Long tenantId = TenantContext.getCurrentTenant();
        Venda venda = vendaRepository.findByIdAndEmpresaIdWithItens(vendaId, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Venda não encontrada."));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Paragraph titulo = new Paragraph("COMPROVANTE DE VENDA #" + venda.getId(), FONT_TITLE);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Cliente: " + venda.getCliente(), FONT_NORMAL));
        document.add(new Paragraph("Data: " + venda.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_NORMAL));
        document.add(new Paragraph("Status: " + venda.getStatusPagamento(), FONT_NORMAL));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4f, 1f, 2f, 2f});

        addHeaderCell(table, "Produto");
        addHeaderCell(table, "Qtd");
        addHeaderCell(table, "Unitário");
        addHeaderCell(table, "Total");

        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        for (ItemVenda item : venda.getItens()) {
            table.addCell(new Phrase(item.getProduto().getNome(), FONT_NORMAL));
            table.addCell(new Phrase(item.getQuantidade().toString(), FONT_NORMAL));
            table.addCell(new Phrase(brl.format(item.getPrecoUnitario()), FONT_NORMAL));
            table.addCell(new Phrase(brl.format(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))), FONT_NORMAL));
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        // 4. Seção Contábil (Subtotal, Desconto e Total)
        PdfPTable tableTotais = new PdfPTable(2);
        tableTotais.setWidthPercentage(100);
        tableTotais.setWidths(new float[]{8f, 2f});

        addTotalRow(tableTotais, "SUBTOTAL:", brl.format(venda.getValorBruto()), FONT_NORMAL);

        if (venda.getValorDesconto() != null && venda.getValorDesconto().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(tableTotais, "DESCONTO (" + venda.getPercentualDesconto() + "%):", "- " + brl.format(venda.getValorDesconto()), FONT_NORMAL);
        }

        addTotalRow(tableTotais, "VALOR FINAL:", brl.format(venda.getValorTotal()), FONT_TITLE);

        document.add(tableTotais);
        document.close();
    }

    // --- MÉTODOS AUXILIARES ---

    private Cliente processarCliente(VendaRequest request, Empresa empresa) {
        if (request.cliente().documento() == null || request.cliente().documento().isBlank()) return null;

        List<Cliente> clientesBase = clienteRepository.buscarPorTermoSeguro(request.cliente().documento());
        Cliente clienteBase;

        if (clientesBase.isEmpty()) {
            clienteBase = Cliente.builder().documento(request.cliente().documento()).build();
            clienteBase.setEmpresa(empresa);
        } else {
            clienteBase = clientesBase.get(0);
            if (!clienteBase.getEmpresa().getId().equals(empresa.getId())) {
                throw new RegraNegocioException("Conflito de Segurança: Documento pertence a outro Tenant.");
            }
        }
        clienteBase.setNome(request.cliente().nome());
        clienteBase.setTelefone(request.cliente().telefone());
        return clienteRepository.save(clienteBase);
    }

    private List<ItemVenda> processarItensEEstoque(VendaRequest request, Empresa empresa, Venda venda) {
        return request.itens().stream().map(dto -> {
            Produto produto = produtoRepository.findById(dto.produtoId())
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado."));

            if (!produto.getEmpresa().getId().equals(empresa.getId())) {
                throw new RegraNegocioException("Acesso Negado: Produto pertence a outro tenant.");
            }
            if (produto.getEstoqueAtual() < dto.quantidade()) {
                throw new RegraNegocioException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            produto.setEstoqueAtual(produto.getEstoqueAtual() - dto.quantidade());
            produtoRepository.save(produto);

            return ItemVenda.builder()
                    .venda(venda).produto(produto).quantidade(dto.quantidade())
                    .precoUnitario(dto.precoUnitario()).build();
        }).toList();
    }

    private BigDecimal calcularTotal(List<ItemVenda> itens) {
        return itens.stream()
                .map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void registrarCaixa(Empresa empresa, Venda venda, TipoMovimentacao tipo) {
        fluxoCaixaService.registrarMovimentacaoInterna(
                empresa,
                (tipo == TipoMovimentacao.ENTRADA ? "Recebimento Venda #" : "Estorno Venda #") + venda.getId() + " - " + venda.getCliente(),
                venda.getValorTotal(), // Usa sempre o valor líquido real (com desconto) para o financeiro
                tipo,
                CategoriaFluxo.EMPRESA
        );
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_HEADER));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }

    private VendaDto converterParaDto(Venda v) {
        List<ItemVendaDto> itensDto = null;
        if (v.getItens() != null) {
            itensDto = v.getItens().stream().map(item -> new ItemVendaDto(
                    item.getProduto().getId(), item.getProduto().getNome(),
                    item.getQuantidade(), item.getPrecoUnitario())).toList();
        }
        return new VendaDto(
                v.getId(),
                v.getCliente(),
                v.getDocumentoCliente(),
                v.getValorBruto(),
                v.getPercentualDesconto(),
                v.getValorDesconto(),
                v.getValorTotal(),
                v.getData(),
                v.getFormaPagamento(),
                v.getStatusPagamento(),
                v.getDataPrevisaoPagamento(),
                itensDto
        );
    }
}