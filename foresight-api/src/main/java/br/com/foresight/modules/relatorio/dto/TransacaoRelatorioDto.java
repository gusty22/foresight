package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoRelatorioDto(
        Long id,
        LocalDateTime data,
        String descricao,
        String cliente,
        String categoria,
        BigDecimal valor,
        String tipo,
        String status
) {}