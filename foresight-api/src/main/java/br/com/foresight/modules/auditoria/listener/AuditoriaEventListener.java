package br.com.foresight.modules.auditoria.listener;

import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import br.com.foresight.modules.auditoria.event.AuditoriaEvent;
import br.com.foresight.modules.auditoria.repository.ILogsAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final ILogsAuditoriaRepository repository;

    /**
     * @Async garante que a thread do usuário não fique esperando o log ser gravado no banco.
     * Propagation.REQUIRES_NEW garante que se a transação do log falhar, não dá rollback na Venda do cliente.
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAuditoriaAssincrona(AuditoriaEvent event) {
        LogsAuditoria log = LogsAuditoria.builder()
                .empresaId(event.tenantId())
                .usuarioEmail(event.usuarioEmail() != null ? event.usuarioEmail() : "SISTEMA")
                .entidadeNome(event.entidadeNome())
                .entidadeId(event.entidadeId())
                .acao(event.acao())
                .detalhes(event.detalhes())
                .dataHora(LocalDateTime.now())
                .build();

        repository.save(log);
    }
}