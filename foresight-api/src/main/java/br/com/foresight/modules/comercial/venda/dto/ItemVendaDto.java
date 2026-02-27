package br.com.foresight.modules.comercial.venda.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemVendaDto(
        @NotNull(message = "O ID do produto é obrigatório")
        Long produtoId,

        String produtoNome,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "O preço unitário é obrigatório")
        BigDecimal precoUnitario
) {}