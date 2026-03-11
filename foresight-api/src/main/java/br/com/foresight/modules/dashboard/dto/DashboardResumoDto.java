package br.com.foresight.modules.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoDto(
        BigDecimal faturamentoMes,
        BigDecimal lucroLiquido,
        int novosClientes,
        int estoqueCritico,
        BigDecimal saldoCaixa,
        BigDecimal metaSobrevivencia,
        BigDecimal percentualMeta,
        List<MovimentacaoRecenteDto> movimentacoesRecentes
) {}