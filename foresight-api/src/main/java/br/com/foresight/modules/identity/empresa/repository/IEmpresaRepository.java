package br.com.foresight.modules.identity.empresa.repository;

import br.com.foresight.modules.identity.empresa.entity.Empresa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEmpresaRepository extends JpaRepository<Empresa, Long> {

    // REGRA SaaS 1:1 - Um usuário tem apenas UMA empresa. Retorna Optional.
    Optional<Empresa> findByDonoId(Long donoId);

    // MÁGICA ENTERPRISE: Acesso estritamente restrito ao dono. Elimina IDOR na raiz.
    Optional<Empresa> findByIdAndDonoId(Long id, Long donoId);

    // MÁGICA ENTERPRISE: Lock Pessimista para evitar Race Conditions financeiras na raiz.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Empresa e WHERE e.id = :id")
    Optional<Empresa> findByIdForUpdate(@Param("id") Long id);
}