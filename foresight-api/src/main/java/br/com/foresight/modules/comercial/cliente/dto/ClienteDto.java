package br.com.foresight.modules.comercial.cliente.dto;

import java.time.LocalDate;

public record ClienteDto(
        Long id,
        String nome,
        String documento,
        String telefone,
        String telefoneAlternativo,
        String email,
        LocalDate dataNascimento,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String estado,
        String tipoCliente,
        String inscricaoEstadual,
        String condicoesEspeciais,
        String observacoes,
        String statusCliente
) {}