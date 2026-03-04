package br.com.foresight.modules.backoffice.auditoria.dto;

import java.time.LocalDateTime;

public record LogAuditoriaDto(
        Long id,
        Long empresaId,
        String dataHoraFormatada,
        LocalDateTime dataHoraRaw,
        String usuarioEmail,
        String acao,
        String entidadeNome,
        Long entidadeId,
        String detalhes
) {}