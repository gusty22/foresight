package br.com.foresight.modules.auditoria.service;

import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import br.com.foresight.modules.auditoria.event.AuditoriaEvent;
import br.com.foresight.modules.auditoria.repository.ILogsAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final ILogsAuditoriaRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAuditoria(AuditoriaEvent event) {
        try {
            LogsAuditoria logEntity = LogsAuditoria.builder()
                    .empresaId(event.tenantId())
                    .usuarioEmail(event.usuarioEmail() != null ? event.usuarioEmail() : "SISTEMA")
                    .entidadeNome(event.entidadeNome())
                    .entidadeId(event.entidadeId())
                    .acao(event.acao())
                    .detalhes(event.detalhes())
                    .dataHora(LocalDateTime.now())
                    .build();

            repository.save(logEntity);
        } catch (Exception e) {
            log.error("FALHA CRÍTICA DE AUDITORIA: Não foi possível salvar o log. Ação: {}", event.acao(), e);
        }
    }
}