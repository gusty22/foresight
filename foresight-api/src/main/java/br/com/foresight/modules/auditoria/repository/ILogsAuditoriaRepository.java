package br.com.foresight.modules.auditoria.repository;

import br.com.foresight.modules.auditoria.entity.LogsAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ILogsAuditoriaRepository extends JpaRepository<LogsAuditoria, Long> {

    // QUERY PARA O BACKOFFICE: Filtra milhões de logs diretamente no banco (Alta Performance)
    @Query("""
        SELECT l FROM LogsAuditoria l 
        WHERE (:termo IS NULL OR LOWER(l.usuarioEmail) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(l.detalhes) LIKE LOWER(CONCAT('%', :termo, '%')))
        AND (:acao IS NULL OR l.acao = :acao)
        AND (cast(:dataInicio as timestamp) IS NULL OR l.dataHora >= :dataInicio)
        AND (cast(:dataFim as timestamp) IS NULL OR l.dataHora <= :dataFim)
    """)
    Page<LogsAuditoria> buscarComFiltrosGlobais(
            @Param("termo") String termo,
            @Param("acao") String acao,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}