package br.com.foresight.modules.backoffice.empresa.dto;

import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import java.time.LocalDateTime;

public record EmpresaGlobalDto(
        Long id,
        String razaoSocial,
        String cnpj,
        StatusEmpresa status,
        String plano,
        LocalDateTime dataCadastro,
        long usuariosAtivos
) {}