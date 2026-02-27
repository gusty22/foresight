package br.com.foresight.modules.comercial.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
        String nome,

        @NotNull(message = "O preço de custo é obrigatório")
        @PositiveOrZero(message = "O preço de custo não pode ser negativo")
        BigDecimal precoCusto,

        @NotNull(message = "O preço de venda é obrigatório")
        @PositiveOrZero(message = "O preço de venda não pode ser negativo")
        BigDecimal precoVenda,

        @PositiveOrZero(message = "O estoque não pode ser negativo")
        Integer estoqueAtual
) {}