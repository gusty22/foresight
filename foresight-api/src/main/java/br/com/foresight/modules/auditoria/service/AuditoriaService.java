package br.com.foresight.modules.auditoria.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.auditoria.dto.LogAuditoriaDto;
import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import br.com.foresight.modules.auditoria.repository.ILogsAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final ILogsAuditoriaRepository repository;

    private Long getTenantIdSeguro() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Falha de segurança: Sessão de tenant inexistente.");
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaDto> listarLogs() {
        Long tenantId = getTenantIdSeguro();
        return repository.findTop100ByEmpresaIdOrderByDataHoraDesc(tenantId)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LogAuditoriaDto buscarPorId(Long id) {
        Long tenantId = getTenantIdSeguro();
        LogsAuditoria log = repository.findByIdAndEmpresaId(id, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Acesso Negado: Registro não encontrado ou pertencente a outra empresa."));
        return converterParaDto(log);
    }

    private LogAuditoriaDto converterParaDto(LogsAuditoria log) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return new LogAuditoriaDto(
                log.getId(),
                log.getDataHora().format(formatter),
                log.getDataHora(),
                log.getUsuarioEmail(),
                log.getAcao(),
                log.getEntidadeNome(),
                log.getEntidadeId(),
                log.getDetalhes()
        );
    }
}