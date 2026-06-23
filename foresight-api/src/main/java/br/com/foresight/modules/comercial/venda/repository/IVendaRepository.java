package br.com.foresight.modules.comercial.venda.repository;

import br.com.foresight.modules.comercial.venda.entity.Venda;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- Import adicionado
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IVendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> { // <-- Interface adicionada aqui

    List<Venda> findAllByEmpresaIdOrderByDataDesc(Long empresaId);

    Optional<Venda> findByIdAndEmpresaId(Long id, Long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Venda v WHERE v.id = :id AND v.empresa.id = :empresaId")
    Optional<Venda> findByIdAndEmpresaIdForUpdate(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("SELECT v FROM Venda v " +
            "JOIN FETCH v.empresa e " +
            "LEFT JOIN FETCH v.itens i " +
            "LEFT JOIN FETCH i.produto p " +
            "WHERE v.id = :id AND e.id = :empresaId")
    Optional<Venda> findByIdAndEmpresaIdWithItens(@Param("id") Long id, @Param("empresaId") Long empresaId);

    // O Dashboard continua usando o `valorTotal` (Líquido) da entidade de Venda PAGA
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.empresa.id = :empresaId AND v.data BETWEEN :inicio AND :fim AND v.statusPagamento = 'PAGO'")
    BigDecimal somarFaturamentoPorPeriodo(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}