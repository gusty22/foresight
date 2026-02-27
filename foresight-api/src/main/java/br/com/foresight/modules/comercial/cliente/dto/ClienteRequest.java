package br.com.foresight.modules.comercial.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
        String nome,

        @Size(max = 20, message = "O documento inválido")
        String documento,

        @Size(max = 20, message = "Telefone inválido")
        String telefone,

        String telefoneAlternativo,

        @Email(message = "E-mail com formato inválido")
        String email,

        LocalDate dataNascimento,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        String cidade,

        @Size(max = 2, message = "O estado deve ter 2 letras (UF)")
        String estado,

        String tipoCliente,
        String inscricaoEstadual,
        String condicoesEspeciais,
        String observacoes,
        String statusCliente
) {}