package br.com.foresight.modules.comercial.investimento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepasseInvestidorDto(
        Long id,
        Long investidorId,
        String investidorNome,
        Long vendaId,
        String produtoNome,
        BigDecimal valorLucroTotal,
        BigDecimal valorRepasse,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataPagamento
) {}