package br.com.foresight.modules.comercial.investimento.repository;

import br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRepasseInvestidorRepository extends JpaRepository<RepasseInvestidor, Long> {
    List<RepasseInvestidor> findAllByEmpresaId(Long empresaId);

    // Usado para listar rapidamente quem a empresa precisa pagar na semana
    List<RepasseInvestidor> findByEmpresaIdAndStatus(Long empresaId, String status);
    List<RepasseInvestidor> findByInvestidorId(Long investidorId);
    // Adicione este método dentro da interface:
    List<RepasseInvestidor> findByInvestidorIdAndStatusOrderByCriadoEmAsc(Long investidorId, String status);
}