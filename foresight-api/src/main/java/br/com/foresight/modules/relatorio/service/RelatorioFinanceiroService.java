package br.com.foresight.modules.relatorio.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.produto.repository.IProdutoRepository;
import br.com.foresight.modules.comercial.venda.entity.Venda;
import br.com.foresight.modules.comercial.venda.repository.IVendaRepository;
import br.com.foresight.modules.financeiro.despesa.entity.Despesa;
import br.com.foresight.modules.financeiro.despesa.repository.IDespesaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.relatorio.dto.DreDto;
import br.com.foresight.modules.relatorio.dto.LucratividadeDto;
import br.com.foresight.modules.relatorio.dto.RankingVendasDto;
import br.com.foresight.modules.relatorio.dto.RelatorioSaudeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioFinanceiroService {

    private final IFluxoCaixaRepository fluxoCaixaRepository;
    private final IDespesaRepository despesaRepository;
    private final IProdutoRepository produtoRepository;
    private final IVendaRepository vendaRepository;

    private Long getTenantIdSeguro() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Sessão inválida. Acesso negado a relatórios financeiros.");
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public DreDto gerarDre(String periodicidade, Integer periodoValor, Integer anoParam) {
        Long tenantId = getTenantIdSeguro();

        int ano = (anoParam != null) ? anoParam : YearMonth.now().getYear();
        LocalDateTime inicio;
        LocalDateTime fim;

        // Configuração dinâmica do período[cite: 1, 2, 3, 4]
        if ("TRIMESTRAL".equalsIgnoreCase(periodicidade)) {
            int trimestre = (periodoValor != null) ? periodoValor : ((LocalDateTime.now().getMonthValue() - 1) / 3) + 1;
            int mesInicio = (trimestre - 1) * 3 + 1;
            int mesFim = mesInicio + 2;
            inicio = LocalDate.of(ano, mesInicio, 1).atStartOfDay();
            fim = YearMonth.of(ano, mesFim).atEndOfMonth().atTime(23, 59, 59);
        } else if ("ANUAL".equalsIgnoreCase(periodicidade)) {
            inicio = LocalDate.of(ano, 1, 1).atStartOfDay();
            fim = LocalDate.of(ano, 12, 31).atTime(23, 59, 59);
        } else { // MENSAL
            int mes = (periodoValor != null) ? periodoValor : LocalDateTime.now().getMonthValue();
            inicio = LocalDate.of(ano, mes, 1).atStartOfDay();
            fim = YearMonth.of(ano, mes).atEndOfMonth().atTime(23, 59, 59);
        }

        // Faturamento Bruto da Atividade
        BigDecimal faturamentoBruto = vendaRepository.somarFaturamentoPorPeriodo(tenantId, inicio, fim);
        if (faturamentoBruto == null) faturamentoBruto = BigDecimal.ZERO;

        // Considerando regime de competência (data de ocorrência)[cite: 1, 2, 3, 4]
        List<Venda> vendasDoPeriodo = vendaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .filter(v -> "PAGO".equalsIgnoreCase(v.getStatusPagamento()) &&
                        !v.getData().isBefore(inicio) && !v.getData().isAfter(fim))
                .toList();

        // Custos (CPV) com a produção/aquisição[cite: 1]
        BigDecimal custosMercadorias = BigDecimal.ZERO;
        for (Venda venda : vendasDoPeriodo) {
            if (venda.getItens() != null) {
                for (var item : venda.getItens()) {
                    if (item.getProduto() != null && item.getProduto().getPrecoCusto() != null) {
                        BigDecimal custoItem = item.getProduto().getPrecoCusto().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        custosMercadorias = custosMercadorias.add(custoItem);
                    }
                }
            }
        }

        // Lucro Bruto após pagar custos primários[cite: 1]
        BigDecimal lucroBruto = faturamentoBruto.subtract(custosMercadorias);

        // Despesas Operacionais
        BigDecimal despesasOperacionais = despesaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .filter(d -> !d.getData().isBefore(inicio) && !d.getData().isAfter(fim))
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (despesasOperacionais == null) despesasOperacionais = BigDecimal.ZERO;

        // Lucro Líquido final indicando a saúde econômica[cite: 1, 2, 3, 4]
        BigDecimal lucroLiquido = lucroBruto.subtract(despesasOperacionais);

        Double margemLiquida = 0.0;
        if (faturamentoBruto.compareTo(BigDecimal.ZERO) > 0) {
            margemLiquida = lucroLiquido.divide(faturamentoBruto, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        return new DreDto(
                faturamentoBruto,
                custosMercadorias,
                lucroBruto,
                despesasOperacionais,
                lucroLiquido,
                margemLiquida
        );
    }

    @Transactional(readOnly = true)
    public List<LucratividadeDto> gerarRelatorioLucratividade() {
        return produtoRepository.findAll().stream().map(produto -> {
            BigDecimal precoVenda = produto.getPrecoVenda() != null ? produto.getPrecoVenda() : BigDecimal.ZERO;
            BigDecimal precoCusto = produto.getPrecoCusto() != null ? produto.getPrecoCusto() : BigDecimal.ZERO;

            BigDecimal lucroUnitario = precoVenda.subtract(precoCusto);

            Double margem = 0.0;
            if (precoVenda.compareTo(BigDecimal.ZERO) > 0) {
                margem = lucroUnitario.divide(precoVenda, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }

            return new LucratividadeDto(
                    produto.getNome(),
                    precoVenda,
                    precoCusto,
                    lucroUnitario,
                    margem,
                    0,
                    BigDecimal.ZERO
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RelatorioSaudeDto calcularSaudeFinanceira() {
        Long tenantId = getTenantIdSeguro();

        BigDecimal saldoPJ = fluxoCaixaRepository.somarPorCategoriaSeguro(tenantId, CategoriaFluxo.EMPRESA);
        if (saldoPJ == null) saldoPJ = BigDecimal.ZERO;

        BigDecimal despesasFixas = despesaRepository.somarDespesasMes(tenantId, LocalDateTime.now().getMonthValue());
        if (despesasFixas == null) despesasFixas = BigDecimal.ZERO;

        BigDecimal reservaSeguranca = saldoPJ.multiply(new BigDecimal("0.10"));
        BigDecimal disponivel = saldoPJ.subtract(despesasFixas).subtract(reservaSeguranca);

        List<String> alertas = new ArrayList<>();
        if (saldoPJ.compareTo(despesasFixas) < 0) {
            alertas.add("ALERTA: Seu saldo atual não cobre as despesas estimadas do mês.");
        }
        if (disponivel.compareTo(BigDecimal.ZERO) < 0) {
            alertas.add("CUIDADO: Você não possui margem livre para retiradas pessoais (Pró-labore).");
        }

        return new RelatorioSaudeDto(
                saldoPJ,
                despesasFixas,
                reservaSeguranca,
                disponivel.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : disponivel,
                alertas
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> calcularPontoEquilibrio() {
        Long tenantId = getTenantIdSeguro();

        BigDecimal despesasFixas = despesaRepository.somarDespesasMes(tenantId, LocalDateTime.now().getMonthValue());
        if (despesasFixas == null) despesasFixas = BigDecimal.ZERO;

        List<LucratividadeDto> produtos = gerarRelatorioLucratividade();
        double margemMedia = produtos.stream()
                .mapToDouble(LucratividadeDto::margemPercentual)
                .average()
                .orElse(0.0) / 100.0;

        BigDecimal faturamentoNecessario = BigDecimal.ZERO;
        if (margemMedia > 0) {
            faturamentoNecessario = despesasFixas.divide(BigDecimal.valueOf(margemMedia), 2, RoundingMode.HALF_UP);
        }

        BigDecimal faturamentoAtual = fluxoCaixaRepository.somarPorCategoriaSeguro(tenantId, CategoriaFluxo.EMPRESA);
        if (faturamentoAtual == null) faturamentoAtual = BigDecimal.ZERO;

        BigDecimal percentual = faturamentoNecessario.compareTo(BigDecimal.ZERO) > 0
                ? faturamentoAtual.divide(faturamentoNecessario, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return Map.of(
                "faturamentoNecessario", faturamentoNecessario,
                "faturamentoAtual", faturamentoAtual,
                "percentualAtingido", percentual.setScale(2, RoundingMode.HALF_UP),
                "faltamParaOLucro", faturamentoNecessario.subtract(faturamentoAtual).max(BigDecimal.ZERO)
        );
    }
    @Transactional(readOnly = true)
    public List<RankingVendasDto> gerarRankingVendas() {
        Long tenantId = getTenantIdSeguro();
        return vendaRepository.rankingDeVendasPorReceita(tenantId);
    }
}