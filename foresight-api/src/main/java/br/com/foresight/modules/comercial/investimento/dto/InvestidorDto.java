package br.com.foresight.modules.comercial.investimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Transformamos o DTO num Request e Num Response misturado (Padrão simples para o seu CRUD)
public record InvestidorDto(
        Long id,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres")
        String nome,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 20, message = "O telefone não pode exceder 20 caracteres")
        String telefone,

        @Size(max = 150, message = "A Chave PIX não pode exceder 150 caracteres")
        String chavePix,

        String status
) {}