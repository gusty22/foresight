package br.com.foresight.modules.dashboard.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.catalogo.repository.IProdutoRepository;
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

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IVendaRepository vendaRepository;
    private final IDespesaRepository despesaRepository;
    private final IProdutoRepository produtoRepository;
    private final IFluxoCaixaRepository fluxoCaixaRepository;

    @Transactional(readOnly = true)
    public DashboardResumoDto obterResumo() {
        // 1. Identificação segura do Tenant via JWT
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Acesso negado. Sessão inválida.");
        }

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        // 2. Faturamento (CORRIGIDO: Passando o tenantId para garantir o isolamento multi-tenant)
        BigDecimal faturamento = vendaRepository.somarFaturamentoPorPeriodo(tenantId, inicioMes, fimMes);
        if (faturamento == null) faturamento = BigDecimal.ZERO;

        // 3. Despesas (Usando o método seguro que busca apenas pela empresa logada)
        BigDecimal despesas = despesaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .filter(d -> !d.isEhPessoal() && d.getData().isAfter(inicioMes) && d.getData().isBefore(fimMes))
                .map(d -> d.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Lucro Líquido
        BigDecimal lucroLiquido = faturamento.subtract(despesas);

        // 5. Estoque Crítico (Produtos com menos de 5 unidades blindados por empresa)
        int estoqueCritico = (int) produtoRepository.findAllByEmpresaId(tenantId).stream()
                .filter(p -> p.getEstoqueAtual() <= 5)
                .count();

        // 6. Saldo e Meta (MVP: Meta de sobrevivência é pagar as contas)
        BigDecimal saldoCaixa = faturamento; // Em um sistema real, viria da tabela consolidada de caixa
        BigDecimal meta = despesas;
        BigDecimal percentualMeta = BigDecimal.ZERO;

        if (meta.compareTo(BigDecimal.ZERO) > 0) {
            percentualMeta = saldoCaixa.divide(meta, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        return new DashboardResumoDto(
                faturamento, lucroLiquido, 0, estoqueCritico, saldoCaixa, meta, percentualMeta, Collections.emptyList()
        );
    }
}