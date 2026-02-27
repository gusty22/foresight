package br.com.foresight.modules.identity.empresa.dto;

import br.com.foresight.modules.identity.empresa.model.TipoEmpresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EmpresaRequest(
        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
        String nome,

        @Size(max = 20, message = "CNPJ inválido")
        String cnpj,

        @Email(message = "E-mail com formato inválido")
        String email,

        @Size(max = 20, message = "Telefone inválido")
        String telefone,

        @Size(max = 10, message = "CEP inválido")
        String cep,

        String logradouro,
        String numero,
        String bairro,
        String cidade,

        @Size(max = 2, message = "O estado deve ter 2 letras (UF)")
        String estado,

        @NotNull(message = "O tipo da empresa é obrigatório")
        TipoEmpresa tipo,

        BigDecimal proLaboreDesejado
) {}