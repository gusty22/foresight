package br.com.foresight.modules.comercial.produto.repository;

import br.com.foresight.modules.comercial.produto.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByEmpresaId(Long empresaId);

    Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);

    // NOVO: Essencial para a funcionalidade do Tablet bipar o produto e já lançar no carrinho
    Optional<Produto> findByEmpresaIdAndCodigoBarras(Long empresaId, String codigoBarras);
}