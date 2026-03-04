package br.com.foresight.modules.backoffice.dashboard.dto;

import java.math.BigDecimal;

public record DashboardGlobalDto(
        long totalEmpresas,
        long empresasAtivas,
        long empresasSuspensas,
        BigDecimal faturamentoMensalEstimado,
        long totalUsuariosGlobais
) {}