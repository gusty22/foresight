package br.com.foresight.modules.identity.auth;

import br.com.foresight.modules.identity.empresa.model.TipoEmpresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "O nome do usuário é obrigatório")
        String nomeUsuario,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotBlank(message = "O telefone é obrigatório")
        String telefoneUsuario,

        @NotBlank(message = "O nome da empresa é obrigatório")
        String nomeEmpresa,

        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpjEmpresa,

        @NotNull(message = "O tipo da empresa é obrigatório")
        TipoEmpresa tipoEmpresa
) {}