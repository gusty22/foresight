package br.com.foresight.modules.comercial.catalogo.dto;

import java.math.BigDecimal;

public record ProdutoDto(
        Long id,
        String nome,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer estoqueAtual,
        BigDecimal margemReal,
        BigDecimal lucroReal,
        String alertaStatus
) {}