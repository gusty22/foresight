package br.com.foresight.modules.relatorio.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.venda.entity.Venda;
import br.com.foresight.modules.comercial.venda.repository.IVendaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.FluxoCaixa;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.relatorio.dto.TransacaoRelatorioDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RelatorioAvancadoService {

    private final IFluxoCaixaRepository fluxoCaixaRepository;
    private final IVendaRepository vendaRepository;

    private Long getTenantId() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RegraNegocioException("Sessão inválida.");
        return tenantId;
    }

    @Transactional(readOnly = true)
    public Page<TransacaoRelatorioDto> buscarDados(
            String contexto, String termo, LocalDate dataInicio, LocalDate dataFim, String tipo, String categoria, Pageable pageable) {

        if ("VENDAS".equalsIgnoreCase(contexto)) {
            // Traduz a ordenação do Frontend para os nomes corretos da entidade Venda
            Pageable pageableVenda = adaptarPageableParaVenda(pageable);

            return vendaRepository.findAll(criarSpecVenda(getTenantId(), termo, dataInicio, dataFim), pageableVenda)
                    .map(this::mapearVenda);
        }

        return fluxoCaixaRepository.findAll(criarSpecFluxo(getTenantId(), termo, dataInicio, dataFim, tipo, categoria), pageable)
                .map(this::mapearFluxo);
    }

    @Transactional(readOnly = true)
    public byte[] exportarParaPdf(String contexto, String termo, LocalDate dataInicio, LocalDate dataFim, String tipo, String categoria) {
        List<TransacaoRelatorioDto> dados;

        if ("VENDAS".equalsIgnoreCase(contexto)) {
            dados = vendaRepository.findAll(criarSpecVenda(getTenantId(), termo, dataInicio, dataFim), Sort.by(Sort.Direction.DESC, "data"))
                    .stream().map(this::mapearVenda).toList();
        } else {
            dados = fluxoCaixaRepository.findAll(criarSpecFluxo(getTenantId(), termo, dataInicio, dataFim, tipo, categoria), Sort.by(Sort.Direction.DESC, "dataHora"))
                    .stream().map(this::mapearFluxo).toList();
        }

        return gerarDocumentoPdf(dados, contexto);
    }

    // ==========================================
    // TRADUTOR DE ORDENAÇÃO (O SEGREDO AQUI!)
    // ==========================================
    private Pageable adaptarPageableParaVenda(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> novasOrdens = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String prop = order.getProperty();

            // Intercepta e traduz o nome da coluna
            if ("dataHora".equals(prop)) {
                prop = "data";
            } else if ("valor".equals(prop)) {
                prop = "valorTotal";
            }

            novasOrdens.add(new Sort.Order(order.getDirection(), prop));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(novasOrdens));
    }

    // ==========================================
    // GERAÇÃO DO ARQUIVO PDF (OPENPDF)
    // ==========================================
    private byte[] gerarDocumentoPdf(List<TransacaoRelatorioDto> dados, String contexto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Relatório Analítico - " + ("VENDAS".equals(contexto) ? "Vendas e Clientes" : "Fluxo de Caixa"), fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.5f, 2f, 1.5f, 1.5f, 1.5f});

            String[] cabecalhos = {"Data", "Origem / Descrição", "Categoria / Cliente", "Tipo", "Status", "Valor"};
            Font fontCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            for (String cabecalho : cabecalhos) {
                PdfPCell cell = new PdfPCell(new Phrase(cabecalho, fontCabecalho));
                cell.setBackgroundColor(new Color(230, 230, 230));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 9);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            BigDecimal totalMovimentado = BigDecimal.ZERO;

            for (TransacaoRelatorioDto dto : dados) {
                table.addCell(new PdfPCell(new Phrase(dto.data().format(dtf), fontDados)));
                table.addCell(new PdfPCell(new Phrase(dto.descricao(), fontDados)));
                table.addCell(new PdfPCell(new Phrase("VENDAS".equals(contexto) ? dto.cliente() : dto.categoria(), fontDados)));

                PdfPCell cellTipo = new PdfPCell(new Phrase(dto.tipo(), fontDados));
                cellTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellTipo);

                PdfPCell cellStatus = new PdfPCell(new Phrase(dto.status(), fontDados));
                cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellStatus);

                String prefixo = "ENTRADA".equals(dto.tipo()) ? "+" : "-";
                totalMovimentado = "ENTRADA".equals(dto.tipo()) ? totalMovimentado.add(dto.valor()) : totalMovimentado.subtract(dto.valor());

                PdfPCell cellValor = new PdfPCell(new Phrase(prefixo + " " + nf.format(dto.valor()), fontDados));
                cellValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellValor);
            }

            document.add(table);

            Paragraph resumo = new Paragraph("Resultado Líquido do Período: " + nf.format(totalMovimentado), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            resumo.setAlignment(Element.ALIGN_RIGHT);
            resumo.setSpacingBefore(20f);
            document.add(resumo);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RegraNegocioException("Erro ao gerar o documento PDF: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPERS E MAPEADORES
    // ==========================================
    private TransacaoRelatorioDto mapearFluxo(FluxoCaixa f) {
        BigDecimal valorAbsoluto = f.getValor() != null ? f.getValor().abs() : BigDecimal.ZERO;
        return new TransacaoRelatorioDto(
                f.getId(), f.getDataHora(), f.getDescricao(), "---",
                f.getCategoriaFluxo() != null ? f.getCategoriaFluxo().name() : "GERAL",
                valorAbsoluto, f.getTipo().name(), "CONSOLIDADO"
        );
    }

    private TransacaoRelatorioDto mapearVenda(Venda v) {
        BigDecimal valorAbsoluto = v.getValorTotal() != null ? v.getValorTotal().abs() : BigDecimal.ZERO;
        return new TransacaoRelatorioDto(
                v.getId(), v.getData(), "Recebimento Venda #" + v.getId(), v.getCliente(),
                "VENDAS", valorAbsoluto, "ENTRADA", v.getStatusPagamento()
        );
    }

    private Specification<FluxoCaixa> criarSpecFluxo(Long tenantId, String termo, LocalDate dataInicio, LocalDate dataFim, String tipo, String categoria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("empresa").get("id"), tenantId));

            if (termo != null && !termo.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("descricao")), "%" + termo.toLowerCase() + "%"));
            }
            if (dataInicio != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), dataInicio.atStartOfDay()));
            if (dataFim != null) predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), dataFim.atTime(LocalTime.MAX)));
            if (tipo != null && !tipo.isBlank()) predicates.add(cb.equal(cb.upper(root.get("tipo").as(String.class)), tipo.toUpperCase()));
            if (categoria != null && !categoria.isBlank()) predicates.add(cb.equal(cb.upper(root.get("categoriaFluxo").as(String.class)), categoria.toUpperCase()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Venda> criarSpecVenda(Long tenantId, String termo, LocalDate dataInicio, LocalDate dataFim) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("empresa").get("id"), tenantId));

            if (termo != null && !termo.isBlank()) {
                String likePattern = "%" + termo.toLowerCase() + "%";
                Predicate nomeCliente = cb.like(cb.lower(root.get("cliente")), likePattern);
                Predicate documento = cb.like(cb.lower(root.get("documentoCliente")), likePattern);
                predicates.add(cb.or(nomeCliente, documento));
            }
            if (dataInicio != null) predicates.add(cb.greaterThanOrEqualTo(root.get("data"), dataInicio.atStartOfDay()));
            if (dataFim != null) predicates.add(cb.lessThanOrEqualTo(root.get("data"), dataFim.atTime(LocalTime.MAX)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}