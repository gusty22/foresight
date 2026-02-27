package br.com.foresight.modules.comercial.catalogo.dto;

import java.math.BigDecimal;

public record SimulacaoPrecoDto(
        BigDecimal precoSugerido,
        BigDecimal margemContribuicao,
        Double margemPercentual,
        BigDecimal novoPontoEquilibrio,
        BigDecimal lucroEstimadoMensal
) {}