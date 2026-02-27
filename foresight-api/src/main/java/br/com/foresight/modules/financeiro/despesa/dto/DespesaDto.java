package br.com.foresight.modules.financeiro.despesa.dto;

import br.com.foresight.modules.financeiro.despesa.entity.TipoDespesa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DespesaDto(
        Long id,
        String descricao,
        String categoria,
        BigDecimal valor,
        LocalDateTime data,
        TipoDespesa tipo,
        boolean ehPessoal,
        String status
) {}