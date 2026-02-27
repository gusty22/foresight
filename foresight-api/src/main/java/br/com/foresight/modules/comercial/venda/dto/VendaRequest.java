package br.com.foresight.modules.comercial.venda.dto;

import br.com.foresight.modules.comercial.cliente.dto.ClienteRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VendaRequest(
        @NotNull(message = "Dados do cliente são obrigatórios")
        @Valid
        ClienteRequest cliente,

        @NotEmpty(message = "A venda deve conter pelo menos um item")
        @Valid
        List<ItemVendaDto> itens,

        @NotBlank(message = "Forma de pagamento é obrigatória")
        String formaPagamento,

        @NotBlank(message = "Status do pagamento é obrigatório")
        String status,

        String dataPrevisao
) {}

