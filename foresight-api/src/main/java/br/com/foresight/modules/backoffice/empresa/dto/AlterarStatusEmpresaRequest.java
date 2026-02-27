package br.com.foresight.modules.backoffice.empresa.dto;

import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusEmpresaRequest(
        @NotNull(message = "O novo status é obrigatório.")
        StatusEmpresa novoStatus,

        String motivo
) {}