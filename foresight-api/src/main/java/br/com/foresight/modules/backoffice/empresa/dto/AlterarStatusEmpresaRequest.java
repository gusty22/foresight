package br.com.foresight.modules.backoffice.empresa.dto;

import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarStatusEmpresaRequest(
        @NotNull(message = "O novo status é obrigatório.")
        StatusEmpresa novoStatus,

        @NotBlank(message = "O motivo da auditoria é obrigatório.")
        @Size(min = 10, max = 255, message = "O motivo deve ter entre 10 e 255 caracteres para validação de auditoria.")
        String motivo
) {}