package br.com.foresight.modules.comercial.investimento.repository;

import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IInvestidorRepository extends JpaRepository<Investidor, Long> {
    List<Investidor> findAllByEmpresaId(Long empresaId);
}