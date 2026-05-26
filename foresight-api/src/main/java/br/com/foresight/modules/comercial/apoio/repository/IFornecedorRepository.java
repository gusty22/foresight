package br.com.foresight.modules.comercial.apoio.repository;

import br.com.foresight.modules.comercial.apoio.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findAllByEmpresaId(Long empresaId);
}