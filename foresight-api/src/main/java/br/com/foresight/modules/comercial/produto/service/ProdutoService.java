package br.com.foresight.modules.comercial.produto.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.apoio.repository.ICategoriaProdutoRepository;
import br.com.foresight.modules.comercial.apoio.repository.IFornecedorRepository;
import br.com.foresight.modules.comercial.investimento.entity.LoteEstoque;
import br.com.foresight.modules.comercial.investimento.repository.ILoteEstoqueRepository;
import br.com.foresight.modules.comercial.investimento.service.LoteEstoqueService;
import br.com.foresight.modules.comercial.produto.dto.ProdutoDto;
import br.com.foresight.modules.comercial.produto.dto.ProdutoRequest;
import br.com.foresight.modules.comercial.produto.entity.Produto;
import br.com.foresight.modules.comercial.produto.repository.IProdutoRepository;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final IProdutoRepository produtoRepository;
    private final IEmpresaRepository empresaRepository;
    private final ICategoriaProdutoRepository categoriaRepository;
    private final IFornecedorRepository fornecedorRepository;
    private final ILoteEstoqueRepository loteEstoqueRepository;
    private final LoteEstoqueService loteEstoqueService;

    private Long getTenantIdSeguro() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Sessão inválida. Empresa não identificada.");
        }
        return tenantId;
    }

    private Empresa getEmpresaLogada() {
        return empresaRepository.findById(getTenantIdSeguro())
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada no banco de dados."));
    }

    private Produto buscarProdutoDaEmpresa(Long id, Long tenantId) {
        return produtoRepository.findByIdAndEmpresaId(id, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Acesso Negado: Produto não encontrado ou pertence a outra empresa."));
    }

    @Transactional
    public ProdutoDto criar(ProdutoRequest dados) {
        Empresa empresa = getEmpresaLogada();

        if (dados.codigoBarras() != null && !dados.codigoBarras().isBlank()) {
            boolean existe = produtoRepository.findByEmpresaIdAndCodigoBarras(empresa.getId(), dados.codigoBarras()).isPresent();
            if (existe) throw new RegraNegocioException("Este código de barras já está cadastrado em outro produto.");
        }

        Produto produto = Produto.builder()
                .nome(dados.nome())
                .codigoBarras(dados.codigoBarras())
                .imagemUrl(dados.imagemUrl())
                .precoCusto(dados.precoCusto())
                .precoVenda(dados.precoVenda())
                .estoqueAtual(dados.estoqueAtual() != null ? dados.estoqueAtual() : 0)
                .estoqueMinimo(dados.estoqueMinimo() != null ? dados.estoqueMinimo() : 5)
                .build();

        if (dados.categoriaId() != null) {
            produto.setCategoria(categoriaRepository.findById(dados.categoriaId()).orElse(null));
        }
        if (dados.fornecedorId() != null) {
            produto.setFornecedor(fornecedorRepository.findById(dados.fornecedorId()).orElse(null));
        }

        produto.setEmpresa(empresa);
        Produto salvo = produtoRepository.save(produto);

        // Criação do lote inicial com base no estoque atual
        if (salvo.getEstoqueAtual() > 0) {
            if (dados.investidorId() != null && dados.percentualLucroInvestidor() != null) {
                loteEstoqueService.criarLoteInvestidor(
                        salvo,
                        salvo.getEstoqueAtual(),
                        dados.investidorId(),
                        dados.percentualLucroInvestidor()
                );
            } else {
                loteEstoqueService.criarLoteInicialProprio(salvo, salvo.getEstoqueAtual());
            }
        }

        return converterParaDto(salvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> listar() {
        return produtoRepository.findAllByEmpresaId(getTenantIdSeguro()).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorId(Long id) {
        return converterParaDto(buscarProdutoDaEmpresa(id, getTenantIdSeguro()));
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorCodigoBarras(String codigoBarras) {
        Produto produto = produtoRepository.findByEmpresaIdAndCodigoBarras(getTenantIdSeguro(), codigoBarras)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado para este código de barras."));
        return converterParaDto(produto);
    }

    @Transactional
    public ProdutoDto atualizar(Long id, ProdutoRequest dados) {
        Produto produto = buscarProdutoDaEmpresa(id, getTenantIdSeguro());

        if (dados.codigoBarras() != null && !dados.codigoBarras().equals(produto.getCodigoBarras())) {
            boolean existe = produtoRepository.findByEmpresaIdAndCodigoBarras(produto.getEmpresa().getId(), dados.codigoBarras()).isPresent();
            if (existe) throw new RegraNegocioException("Este código de barras já está cadastrado em outro produto.");
        }

        produto.setNome(dados.nome());
        produto.setCodigoBarras(dados.codigoBarras());
        produto.setImagemUrl(dados.imagemUrl());
        produto.setPrecoCusto(dados.precoCusto());
        produto.setPrecoVenda(dados.precoVenda());
        produto.setEstoqueAtual(dados.estoqueAtual() != null ? dados.estoqueAtual() : 0);
        produto.setEstoqueMinimo(dados.estoqueMinimo() != null ? dados.estoqueMinimo() : 5);

        if (dados.categoriaId() != null) {
            produto.setCategoria(categoriaRepository.findById(dados.categoriaId()).orElse(null));
        } else {
            produto.setCategoria(null);
        }

        if (dados.fornecedorId() != null) {
            produto.setFornecedor(fornecedorRepository.findById(dados.fornecedorId()).orElse(null));
        } else {
            produto.setFornecedor(null);
        }

        // Não altera os campos de investidor (lote já existente)

        return converterParaDto(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarProdutoDaEmpresa(id, getTenantIdSeguro());
        produtoRepository.delete(produto);
    }

    private ProdutoDto converterParaDto(Produto p) {
        BigDecimal margem = p.calcularMargem();
        BigDecimal lucro = p.getLucroUnidade();

        String alerta = "OK";
        if (margem.compareTo(BigDecimal.ZERO) < 0) {
            alerta = "PREJUIZO";
        } else if (p.getEstoqueAtual() <= (p.getEstoqueMinimo() != null ? p.getEstoqueMinimo() : 5)) {
            alerta = "CRITICO";
        }

        // Busca o primeiro lote (o mais antigo) para obter dados do investidor
        LoteEstoque loteInicial = loteEstoqueRepository.findFirstByProdutoOrderByDataEntradaAsc(p).orElse(null);
        Long investidorId = null;
        String investidorNome = null;
        BigDecimal percentual = null;
        if (loteInicial != null && loteInicial.getInvestidor() != null) {
            investidorId = loteInicial.getInvestidor().getId();
            investidorNome = loteInicial.getInvestidor().getNome();
            percentual = loteInicial.getPercentualLucroInvestidor();
        }

        return new ProdutoDto(
                p.getId(),
                p.getNome(),
                p.getCodigoBarras(),
                p.getImagemUrl(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNome() : null,
                p.getFornecedor() != null ? p.getFornecedor().getId() : null,
                p.getFornecedor() != null ? p.getFornecedor().getNome() : null,
                p.getPrecoCusto(),
                p.getPrecoVenda(),
                p.getEstoqueAtual(),
                p.getEstoqueMinimo(),
                margem,
                lucro,
                alerta,
                investidorId,
                investidorNome,
                percentual
        );
    }
}