package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;

public record RankingVendasDto(
        String produtoNome,
        Long quantidadeVendida,
        BigDecimal receitaTotal
) {}