package br.com.foresight.modules.comercial.venda.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vendas")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venda extends BaseTenantEntity {

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nome_historico", nullable = false, length = 150)
    private String cliente;

    @Column(name = "documento_cliente", length = 20)
    private String documentoCliente;

    @Column(name = "telefone_cliente", length = 20)
    private String telefoneCliente;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "status_pagamento", length = 20)
    private String statusPagamento;

    @Column(name = "data_previsao_pagamento")
    private LocalDate dataPrevisaoPagamento;

    @Column(nullable = false)
    private LocalDateTime data;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens;
}
