package br.com.foresight.modules.identity.usuario.dto;

import br.com.foresight.modules.identity.empresa.dto.EmpresaDto;

public record PerfilUsuarioDto(
        Long id,
        String nome,
        String email,
        String telefone,
        EmpresaDto empresa
) {}