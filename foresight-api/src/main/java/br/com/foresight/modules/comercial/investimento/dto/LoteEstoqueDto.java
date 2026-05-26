package br.com.foresight.modules.comercial.investimento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteEstoqueDto(
        Long id,
        String produtoNome,
        String investidorNome,
        Integer quantidadeInicial,
        Integer quantidadeDisponivel,
        BigDecimal custoUnitario, // NOVO
        BigDecimal percentualLucro, // NOVO
        String status,
        LocalDateTime dataEntrada
) {}