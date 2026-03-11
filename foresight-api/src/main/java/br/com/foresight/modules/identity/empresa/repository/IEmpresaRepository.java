package br.com.foresight.modules.identity.empresa.repository;

import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByDonoId(Long donoId);
    Optional<Empresa> findByIdAndDonoId(Long id, Long donoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Empresa e WHERE e.id = :id")
    Optional<Empresa> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(StatusEmpresa status);

    @Query("SELECT e FROM Empresa e WHERE LOWER(e.nome) LIKE %:termo% OR e.cnpj LIKE %:termo%")
    Page<Empresa> buscarPorTermoPaginado(@Param("termo") String termo, Pageable pageable);
}