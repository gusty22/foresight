package br.com.foresight.modules.dashboard.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.produto.repository.IProdutoRepository;
import br.com.foresight.modules.comercial.venda.entity.Venda;
import br.com.foresight.modules.comercial.venda.repository.IVendaRepository;
import br.com.foresight.modules.financeiro.despesa.repository.IDespesaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.dashboard.dto.DashboardResumoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IVendaRepository vendaRepository;
    private final IDespesaRepository despesaRepository;
    private final IProdutoRepository produtoRepository;
    private final IFluxoCaixaRepository fluxoCaixaRepository;

    @Transactional(readOnly = true)
    public DashboardResumoDto obterResumo() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Acesso negado. Sessão inválida.");
        }

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        // 1. Faturamento
        BigDecimal faturamento = vendaRepository.somarFaturamentoPorPeriodo(tenantId, inicioMes, fimMes);
        if (faturamento == null) faturamento = BigDecimal.ZERO;

        // 2. CPV (Custo do Produto Vendido) - CORREÇÃO CONTÁBIL
        // Busca todas as vendas PAGAS do mês para somar o custo das mercadorias que saíram do estoque
        List<Venda> vendasDoMes = vendaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .filter(v -> "PAGO".equalsIgnoreCase(v.getStatusPagamento()) &&
                        v.getData().isAfter(inicioMes) && v.getData().isBefore(fimMes))
                .toList();

        BigDecimal custoMercadorias = BigDecimal.ZERO;
        for (Venda venda : vendasDoMes) {
            if (venda.getItens() != null) {
                for (var item : venda.getItens()) {
                    if (item.getProduto() != null && item.getProduto().getPrecoCusto() != null) {
                        BigDecimal custoDoItem = item.getProduto().getPrecoCusto().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        custoMercadorias = custoMercadorias.add(custoDoItem);
                    }
                }
            }
        }

        // 3. Despesas Operacionais
        BigDecimal despesas = despesaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .filter(d -> !d.isEhPessoal() && d.getData().isAfter(inicioMes) && d.getData().isBefore(fimMes))
                .map(d -> d.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Lucro Líquido Real = Faturamento - CPV - Despesas
        BigDecimal lucroLiquido = faturamento.subtract(custoMercadorias).subtract(despesas);

        // 5. Estoque Crítico
        int estoqueCritico = (int) produtoRepository.findAllByEmpresaId(tenantId).stream()
                .filter(p -> p.getEstoqueAtual() <= 5)
                .count();

        // 6. Saldo e Meta
        // O Saldo em caixa real deveria considerar as movimentações anteriores, mas mantemos o MVP atual
        BigDecimal saldoCaixa = faturamento;

        // A Meta de Sobrevivência é pagar o Custo das Mercadorias + Despesas Fixas
        BigDecimal meta = despesas.add(custoMercadorias);

        BigDecimal percentualMeta = BigDecimal.ZERO;
        if (meta.compareTo(BigDecimal.ZERO) > 0) {
            percentualMeta = saldoCaixa.divide(meta, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        return new DashboardResumoDto(
                faturamento, lucroLiquido, 0, estoqueCritico, saldoCaixa, meta, percentualMeta, Collections.emptyList()
        );
    }
}