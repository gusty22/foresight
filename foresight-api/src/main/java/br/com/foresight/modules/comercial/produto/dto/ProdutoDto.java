package br.com.foresight.modules.comercial.produto.dto;

import java.math.BigDecimal;

public record ProdutoDto(
        Long id,
        String nome,
        String categoria,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer estoqueAtual,
        Integer estoqueMinimo,
        BigDecimal margemReal,
        BigDecimal lucroReal,
        String alertaStatus
) {}