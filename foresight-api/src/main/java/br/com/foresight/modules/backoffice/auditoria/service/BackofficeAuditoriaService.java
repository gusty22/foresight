package br.com.foresight.modules.backoffice.auditoria.service;

import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import br.com.foresight.modules.auditoria.repository.ILogsAuditoriaRepository;
import br.com.foresight.modules.backoffice.auditoria.dto.LogAuditoriaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BackofficeAuditoriaService {

    private final ILogsAuditoriaRepository repository;

    @Transactional(readOnly = true)
    public Page<LogAuditoriaDto> listarLogsGlobais(String termo, String acao, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {

        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        String termoBusca = (termo != null && !termo.isBlank()) ? termo : null;
        String acaoBusca = (acao != null && !acao.isBlank() && !acao.equals("TODAS")) ? acao : null;

        Page<LogsAuditoria> logs = repository.buscarComFiltrosGlobais(termoBusca, acaoBusca, inicio, fim, pageable);

        return logs.map(this::converterParaDto);
    }

    private LogAuditoriaDto converterParaDto(LogsAuditoria log) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return new LogAuditoriaDto(
                log.getId(),
                log.getEmpresaId(),
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