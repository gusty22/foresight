package br.com.foresight.modules.auditoria.listener;

import br.com.foresight.modules.auditoria.event.AuditoriaEvent;
import br.com.foresight.modules.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final AuditoriaService auditoriaService;

    @Async
    @EventListener
    public void handleAuditoriaEvent(AuditoriaEvent event) {
        auditoriaService.registrarAuditoria(event);
    }
}