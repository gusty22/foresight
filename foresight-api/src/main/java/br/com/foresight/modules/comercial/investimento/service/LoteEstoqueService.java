package br.com.foresight.modules.comercial.investimento.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.investimento.dto.LoteEstoqueDto;
import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import br.com.foresight.modules.comercial.investimento.entity.LoteEstoque;
import br.com.foresight.modules.comercial.investimento.repository.ILoteEstoqueRepository;
import br.com.foresight.modules.comercial.investimento.repository.IInvestidorRepository;
import br.com.foresight.modules.comercial.produto.entity.Produto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoteEstoqueService {

    private final ILoteEstoqueRepository loteEstoqueRepository;
    private final IInvestidorRepository investidorRepository;

    // ========== Criação de lotes ==========
    @Transactional
    public void criarLoteInicialProprio(Produto produto, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) return;
        LoteEstoque lote = LoteEstoque.builder()
                .produto(produto)
                .investidor(null)
                .quantidadeInicial(quantidade)
                .quantidadeDisponivel(quantidade)
                .custoUnitario(produto.getPrecoCusto())
                .percentualLucroInvestidor(BigDecimal.ZERO)
                .dataEntrada(LocalDateTime.now())
                .status("ABERTO")
                .build();
        lote.setEmpresa(produto.getEmpresa());
        loteEstoqueRepository.save(lote);
    }

    @Transactional
    public void criarLoteInvestidor(Produto produto, Integer quantidade, Long investidorId, BigDecimal percentualLucro) {
        Investidor investidor = investidorRepository.findById(investidorId)
                .orElseThrow(() -> new RegraNegocioException("Investidor não encontrado"));
        LoteEstoque lote = LoteEstoque.builder()
                .produto(produto)
                .investidor(investidor)
                .quantidadeInicial(quantidade)
                .quantidadeDisponivel(quantidade)
                .custoUnitario(produto.getPrecoCusto())
                .percentualLucroInvestidor(percentualLucro)
                .dataEntrada(LocalDateTime.now())
                .status("ABERTO")
                .build();
        lote.setEmpresa(produto.getEmpresa());
        loteEstoqueRepository.save(lote);
    }

    // ========== Baixa de estoque ==========
    @Transactional
    public void subtrairEstoqueDoLote(LoteEstoque lote, Integer quantidadeVenda) {
        if (lote.getQuantidadeDisponivel() < quantidadeVenda) {
            throw new RegraNegocioException("Falha sistêmica: Tentativa de subtrair mais estoque do que o Lote permite.");
        }
        lote.setQuantidadeDisponivel(lote.getQuantidadeDisponivel() - quantidadeVenda);
        if (lote.getQuantidadeDisponivel() == 0) {
            lote.setStatus("FINALIZADO");
        }
        loteEstoqueRepository.save(lote);
    }

    // ========== Listagem para a tela de Estoques Financiados ==========
    @Transactional(readOnly = true)  // ← ESSENCIAL para manter sessão aberta
    public List<LoteEstoqueDto> listarTodosDaEmpresa() {
        Long empresaId = TenantContext.getCurrentTenant();
        return loteEstoqueRepository.findAllByEmpresaIdWithProduto(empresaId).stream()
                // Onde você faz o .map() para converter a Entidade em DTO, atualize para:
                .map(lote -> new LoteEstoqueDto(
                        lote.getId(),
                        lote.getProduto().getNome(),
                        lote.getInvestidor() != null ? lote.getInvestidor().getNome() : "Capital Próprio",
                        lote.getQuantidadeInicial(),
                        lote.getQuantidadeDisponivel(),
                        lote.getCustoUnitario(), // Adicionado
                        lote.getPercentualLucroInvestidor(), // Adicionado
                        lote.getStatus(),
                        lote.getDataEntrada()
                ))
                .collect(Collectors.toList());
    }
}