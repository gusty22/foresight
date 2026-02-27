package br.com.foresight.modules.comercial.catalogo.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.catalogo.dto.ProdutoDto;
import br.com.foresight.modules.comercial.catalogo.dto.ProdutoRequest;
import br.com.foresight.modules.comercial.catalogo.entity.Produto;
import br.com.foresight.modules.comercial.catalogo.repository.IProdutoRepository;
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

        Produto produto = Produto.builder()
                .nome(dados.nome())
                .precoCusto(dados.precoCusto())
                .precoVenda(dados.precoVenda())
                .estoqueAtual(dados.estoqueAtual() != null ? dados.estoqueAtual() : 0)
                .build();

        produto.setEmpresa(empresa);

        return converterParaDto(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoDto> listar() {
        return produtoRepository.findAllByEmpresaId(getTenantIdSeguro()).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscarPorId(Long id) {
        Produto produto = buscarProdutoDaEmpresa(id, getTenantIdSeguro());
        return converterParaDto(produto);
    }

    @Transactional
    public ProdutoDto atualizar(Long id, ProdutoRequest dados) {
        Produto produto = buscarProdutoDaEmpresa(id, getTenantIdSeguro());

        produto.setNome(dados.nome());
        produto.setPrecoCusto(dados.precoCusto());
        produto.setPrecoVenda(dados.precoVenda());
        produto.setEstoqueAtual(dados.estoqueAtual() != null ? dados.estoqueAtual() : 0);

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
        String alerta = margem.compareTo(BigDecimal.ZERO) < 0 ? "PREJUIZO" : "OK";

        return new ProdutoDto(
                p.getId(),
                p.getNome(),
                p.getPrecoCusto(),
                p.getPrecoVenda(),
                p.getEstoqueAtual(),
                margem,
                lucro,
                alerta
        );
    }
}