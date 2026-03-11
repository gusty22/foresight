package br.com.foresight.modules.comercial.cliente.repository;

import br.com.foresight.modules.comercial.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {
    @Query("SELECT c FROM Cliente c WHERE c.nome ILIKE %:termo% OR c.documento ILIKE %:termo%")
    List<Cliente> buscarPorTermoSeguro(@Param("termo") String termo);
}