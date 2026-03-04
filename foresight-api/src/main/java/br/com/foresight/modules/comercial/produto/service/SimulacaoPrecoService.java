package br.com.foresight.modules.comercial.produto.service;

import br.com.foresight.modules.comercial.produto.dto.SimulacaoPrecoDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SimulacaoPrecoService {

    public SimulacaoPrecoDto simular(BigDecimal custo, BigDecimal precoVenda, BigDecimal despesasFixas) {
        BigDecimal margemContribuicao = precoVenda.subtract(custo);

        Double margemPercentual = 0.0;
        if (precoVenda.compareTo(BigDecimal.ZERO) > 0) {
            margemPercentual = margemContribuicao.divide(precoVenda, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        BigDecimal novoPE = BigDecimal.ZERO;
        if (margemPercentual > 0) {
            novoPE = despesasFixas.divide(BigDecimal.valueOf(margemPercentual / 100.0), 2, RoundingMode.HALF_UP);
        }

        return new SimulacaoPrecoDto(
                precoVenda,
                margemContribuicao,
                margemPercentual,
                novoPE,
                margemContribuicao.multiply(new BigDecimal("100"))
        );
    }
}