package br.com.foresight.modules.comercial.produto.dto;

import java.math.BigDecimal;

public record SimulacaoPrecoDto(
        BigDecimal precoSugerido,
        BigDecimal margemContribuicao,
        Double margemPercentual,
        BigDecimal novoPontoEquilibrio,
        BigDecimal lucroEstimadoMensal
) {}