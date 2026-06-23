package br.com.foresight.modules.financeiro.fluxo_caixa.repository;

import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.FluxoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFluxoCaixaRepository extends JpaRepository<FluxoCaixa, Long>, JpaSpecificationExecutor<FluxoCaixa> {

    List<FluxoCaixa> findByEmpresaIdOrderByDataHoraDesc(Long empresaId);

    Optional<FluxoCaixa> findTopByEmpresaIdOrderByDataHoraDesc(Long empresaId);

    Optional<FluxoCaixa> findByOrigemAndOrigemIdAndEmpresaId(String origem, Long origemId, Long empresaId);

    // BLINDAGEM MATEMÁTICA: Força Entradas como positivas e Saídas como negativas (ABS)
    @Query("SELECT COALESCE(SUM(CASE WHEN f.tipo = 'ENTRADA' THEN ABS(f.valor) ELSE -ABS(f.valor) END), 0) " +
            "FROM FluxoCaixa f " +
            "WHERE f.empresa.id = :empresaId " +
            "AND f.categoriaFluxo = :categoria " +
            "AND f.estornado = false")
    BigDecimal somarPorCategoriaSeguro(@Param("empresaId") Long empresaId, @Param("categoria") CategoriaFluxo categoria);
}