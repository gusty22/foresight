package br.com.foresight.modules.financeiro.fluxo_caixa.dto;

import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FluxoCaixaRequest(
        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser estritamente positivo (maior que zero)")
        BigDecimal valor,

        @NotNull(message = "O tipo da movimentação é obrigatório")
        TipoMovimentacao tipo,

        @NotNull(message = "A categoria do fluxo é obrigatória")
        CategoriaFluxo categoriaFluxo
) {}