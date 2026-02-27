package br.com.foresight.modules.financeiro.fluxo_caixa.dto;

import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FluxoCaixaDto(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoMovimentacao tipo,
        BigDecimal saldoAposMovimentacao,
        LocalDateTime dataHora,
        CategoriaFluxo categoriaFluxo,
        boolean estornado,
        Long referenciaEstornoId
) {}