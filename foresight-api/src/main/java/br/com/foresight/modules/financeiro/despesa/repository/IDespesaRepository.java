package br.com.foresight.modules.financeiro.despesa.repository;

import br.com.foresight.modules.financeiro.despesa.entity.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDespesaRepository extends JpaRepository<Despesa, Long> {

    Optional<Despesa> findByIdAndEmpresaId(Long id, Long empresaId);
    List<Despesa> findAllByEmpresaIdOrderByDataDesc(Long empresaId);

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.empresa.id = :empresaId AND d.ehPessoal = false AND EXTRACT(MONTH FROM d.data) = :mes")
    BigDecimal somarDespesasMes(@Param("empresaId") Long empresaId, @Param("mes") int mes);
}