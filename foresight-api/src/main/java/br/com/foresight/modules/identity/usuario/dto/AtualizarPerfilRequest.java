package br.com.foresight.modules.identity.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "Nome muito longo")
        String nome,

        @Size(max = 20, message = "Telefone inválido")
        String telefone
) {}