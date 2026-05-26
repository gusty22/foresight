package br.com.foresight.modules.comercial.investimento.repository;

import br.com.foresight.modules.comercial.investimento.entity.LoteEstoque;
import br.com.foresight.modules.comercial.produto.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILoteEstoqueRepository extends JpaRepository<LoteEstoque, Long> {

    // 🔹 Com JOIN FETCH para evitar LazyInitializationException
    @Query("SELECT l FROM LoteEstoque l " +
            "JOIN FETCH l.produto p " +
            "LEFT JOIN FETCH l.investidor i " +
            "WHERE l.empresa.id = :empresaId")
    List<LoteEstoque> findAllByEmpresaIdWithProduto(@Param("empresaId") Long empresaId);

    // Método para o VendaService (FIFO) – também com FETCH para evitar problemas
    @Query("SELECT l FROM LoteEstoque l " +
            "JOIN FETCH l.produto p " +
            "LEFT JOIN FETCH l.investidor i " +
            "WHERE l.produto.id = :produtoId AND l.quantidadeDisponivel > 0 " +
            "ORDER BY l.dataEntrada ASC")
    List<LoteEstoque> findLotesDisponiveisPorProduto(@Param("produtoId") Long produtoId);

    // Mantém para compatibilidade
    default List<LoteEstoque> findByProdutoIdAndQuantidadeDisponivelGreaterThanOrderByDataEntradaAsc(Long produtoId, int quantidade) {
        return findLotesDisponiveisPorProduto(produtoId);
    }

    // Busca o primeiro lote do produto (com FETCH)
    @Query("SELECT l FROM LoteEstoque l " +
            "JOIN FETCH l.produto p " +
            "LEFT JOIN FETCH l.investidor i " +
            "WHERE l.produto = :produto ORDER BY l.dataEntrada ASC")
    Optional<LoteEstoque> findFirstByProdutoWithFetch(@Param("produto") Produto produto);

    default Optional<LoteEstoque> findFirstByProdutoOrderByDataEntradaAsc(Produto produto) {
        return findFirstByProdutoWithFetch(produto);
    }
    List<LoteEstoque> findByInvestidorId(Long investidorId);
}