package br.com.foresight.modules.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoRecenteDto(
        String descricao,
        String categoria,
        LocalDateTime data,
        BigDecimal valor,
        String tipo
) {}