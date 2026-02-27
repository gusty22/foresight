package br.com.foresight.modules.auditoria.dto;

import java.time.LocalDateTime;

public record LogAuditoriaDto(
        Long id,
        String dataHoraFormatada,
        LocalDateTime dataHoraRaw,
        String usuarioEmail,
        String acao,
        String entidadeNome,
        Long entidadeId,
        String detalhes
) {}