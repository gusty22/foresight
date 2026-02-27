package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;

public record LucratividadeDto(
        String produtoNome,
        BigDecimal precoVenda,
        BigDecimal precoCusto,
        BigDecimal lucroUnitario,
        Double margemPercentual,
        Integer quantidadeVendida,
        BigDecimal lucroTotalAcumulado
) {}