package br.com.foresight.modules.backoffice.dashboard.service;

import br.com.foresight.modules.backoffice.dashboard.dto.DashboardGlobalDto;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackofficeDashboardService {

    private final IEmpresaRepository empresaRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public DashboardGlobalDto calcularResumoGlobal() {
        log.info("Calculando métricas globais do Data Center...");

        long totalEmpresas = empresaRepository.count();
        long empresasAtivas = empresaRepository.countByStatus(StatusEmpresa.ATIVA);
        long empresasSuspensas = totalEmpresas - empresasAtivas;
        long totalUsuarios = usuarioRepository.count();

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