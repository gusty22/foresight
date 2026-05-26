package br.com.foresight.modules.comercial.apoio.repository;

import br.com.foresight.modules.comercial.apoio.entity.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoriaProdutoRepository extends JpaRepository<CategoriaProduto, Long> {
    List<CategoriaProduto> findAllByEmpresaId(Long empresaId);
}