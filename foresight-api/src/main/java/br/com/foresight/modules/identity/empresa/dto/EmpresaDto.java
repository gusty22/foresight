package br.com.foresight.modules.identity.empresa.dto;

import br.com.foresight.modules.identity.empresa.model.TipoEmpresa;
import java.math.BigDecimal;

public record EmpresaDto(
        Long id,
        String nome,
        String cnpj,
        String email,
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String estado,
        BigDecimal proLaboreDesejado,
        TipoEmpresa tipo
) {}