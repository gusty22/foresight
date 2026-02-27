package br.com.foresight.modules.auditoria.event;

public record AuditoriaEvent(
        Long tenantId,
        String usuarioEmail,
        String entidadeNome,
        Long entidadeId,
        String acao,
        String detalhes
) {}