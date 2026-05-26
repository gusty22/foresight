package br.com.foresight.modules.comercial.investimento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PagamentoInvestidorRequest(
        @NotNull(message = "O valor do pagamento é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor
) {}