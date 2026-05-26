package br.com.foresight.modules.comercial.investimento.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import br.com.foresight.modules.comercial.venda.entity.ItemVenda;
import br.com.foresight.modules.comercial.venda.entity.Venda;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repasses_investidor")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepasseInvestidor extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investidor_id", nullable = false)
    private Investidor investidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_venda_id", nullable = false)
    private ItemVenda itemVenda;

    @Column(name = "valor_lucro_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorLucroTotal;

    @Column(name = "valor_repasse", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorRepasse;

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
}