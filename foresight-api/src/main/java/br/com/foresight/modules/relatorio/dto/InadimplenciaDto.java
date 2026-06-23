package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InadimplenciaDto(
        Long vendaId,
        String clienteNome,
        LocalDate dataVencimento,
        Long diasAtraso,
        BigDecimal valorDevido,
        String status
) {}