package br.com.foresight.modules.backoffice.dashboard.service;

import br.com.foresight.modules.backoffice.dashboard.dto.DashboardGlobalDto;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BackofficeDashboardService {

    private final IEmpresaRepository empresaRepository;
    private final IUsuarioRepository usuarioRepository;

    /**
     * Calcula o resumo da plataforma para o Super Admin.
     * Como Empresa e Usuario são raízes, o Hibernate não aplica filtros de tenant aqui.
     */
    @Transactional(readOnly = true)
    public DashboardGlobalDto calcularResumoGlobal() {

        long totalEmpresas = empresaRepository.count();

        // Contagem filtrada por status (SaaS Health)
        long empresasAtivas = empresaRepository.findAll().stream()
                .filter(e -> e.getStatus() == StatusEmpresa.ATIVA)
                .count();

        long empresasSuspensas = totalEmpresas - empresasAtivas;

        long totalUsuarios = usuarioRepository.count();

        // Cálculo de Faturamento Estimado (Exemplo: Baseado no total de empresas ativas)
        // Em um SaaS real, você somaria os valores das assinaturas vigentes.
        BigDecimal ticketMedioMensal = new BigDecimal("99.90");
        BigDecimal faturamentoEstimado = ticketMedioMensal.multiply(BigDecimal.valueOf(empresasAtivas));

        return new DashboardGlobalDto(
                totalEmpresas,
                empresasAtivas,
                empresasSuspensas,
                faturamentoEstimado,
                totalUsuarios
        );
    }
}