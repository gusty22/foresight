package br.com.foresight.modules.financeiro.fluxo_caixa.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fluxo_caixa")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FluxoCaixa extends BaseTenantEntity {

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TipoMovimentacao tipo;

    @Column(name = "saldo_apos_movimentacao", nullable = false, updatable = false)
    private BigDecimal saldoAposMovimentacao;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_fluxo", length = 50, updatable = false)
    private CategoriaFluxo categoriaFluxo = CategoriaFluxo.EMPRESA;

    @Builder.Default
    @Column(nullable = false)
    private boolean estornado = false;

    @Column(name = "referencia_estorno_id")
    private Long referenciaEstornoId;

    @Column(name = "origem", length = 50, updatable = false)
    private String origem;

    @Column(name = "origem_id", updatable = false)
    private Long origemId;

    @Version
    private Long version;
}