package br.com.foresight.modules.relatorio.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.catalogo.repository.IProdutoRepository;
import br.com.foresight.modules.financeiro.despesa.repository.IDespesaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.relatorio.dto.DreDto;
import br.com.foresight.modules.relatorio.dto.LucratividadeDto;
import br.com.foresight.modules.relatorio.dto.RelatorioSaudeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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

    private Long getTenantIdSeguro() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Sessão inválida. Acesso negado a relatórios financeiros.");
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public DreDto gerarDreMensal(int mes) {
        Long tenantId = getTenantIdSeguro();

        // 1. Faturamento Total
        BigDecimal faturamento = fluxoCaixaRepository.somarPorCategoriaSeguro(tenantId, CategoriaFluxo.EMPRESA);
        if (faturamento == null) faturamento = BigDecimal.ZERO;

        // 2. Despesas (Corrigido com envio do tenantId)
        BigDecimal despesasOperacionais = despesaRepository.somarDespesasMes(tenantId, mes);
        if (despesasOperacionais == null) despesasOperacionais = BigDecimal.ZERO;

        // 3. Lucro
        BigDecimal lucroLiquido = faturamento.subtract(despesasOperacionais);

        Double margemLiquida = 0.0;
        if (faturamento.compareTo(BigDecimal.ZERO) > 0) {
            margemLiquida = lucroLiquido.divide(faturamento, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        return new DreDto(
                faturamento,
                BigDecimal.ZERO,
                faturamento,
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

        // Corrigido com envio do tenantId
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

        // Corrigido com envio do tenantId
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
}