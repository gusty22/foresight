package br.com.foresight.modules.relatorio.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioSaudeDto(
        BigDecimal saldoCaixa,
        BigDecimal despesasComprometidas,
        BigDecimal reservaSeguranca,
        BigDecimal disponivelParaVoce,
        List<String> alertasCriticos
) {}