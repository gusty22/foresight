package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;

public record DreDto(
        BigDecimal faturamentoTotal,
        BigDecimal custosProdutosVendidos,
        BigDecimal lucroBruto,
        BigDecimal despesasOperacionais,
        BigDecimal lucroLiquido,
        Double margemLiquidaPercentual
) {}