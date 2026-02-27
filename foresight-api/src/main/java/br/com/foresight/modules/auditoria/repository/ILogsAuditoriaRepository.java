package br.com.foresight.modules.auditoria.repository;

import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILogsAuditoriaRepository extends JpaRepository<LogsAuditoria, Long> {

    // Proteção rigorosa por Tenant
    List<LogsAuditoria> findTop100ByEmpresaIdOrderByDataHoraDesc(Long empresaId);

    Optional<LogsAuditoria> findByIdAndEmpresaId(Long id, Long empresaId);
}