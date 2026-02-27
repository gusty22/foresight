package br.com.foresight.modules.financeiro.despesa.dto;

import br.com.foresight.modules.financeiro.despesa.entity.TipoDespesa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DespesaRequest(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição não pode exceder 255 caracteres")
        String descricao,

        @Size(max = 100, message = "A categoria não pode exceder 100 caracteres")
        String categoria,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O tipo da despesa é obrigatório")
        TipoDespesa tipo,

        boolean ehPessoal,

        @NotNull(message = "A data é obrigatória")
        LocalDateTime data
) {}