package br.com.foresight.modules.comercial.investimento.dto;

import java.math.BigDecimal;
import java.util.List;

public record RelatorioInvestidorDto(
        InvestidorDto investidor,
        BigDecimal totalInvestido,
        BigDecimal totalLucroGeradoGeral,
        BigDecimal totalRepassePendente,
        BigDecimal totalRepassePago,
        List<LoteEstoqueDto> lotesAtivos,
        List<RepasseInvestidorDto> repasses
) {}