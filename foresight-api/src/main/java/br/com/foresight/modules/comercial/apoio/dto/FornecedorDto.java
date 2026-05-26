package br.com.foresight.modules.comercial.apoio.dto;

public record FornecedorDto(
        Long id,
        String nome,
        String telefone,
        String cnpj
) {}