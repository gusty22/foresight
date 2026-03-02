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

    // NOVO: Valor da soma dos itens antes do desconto
    @Column(name = "valor_bruto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBruto;

    // NOVO: Porcentagem informada pelo usuário (Ex: 10.00)
    @Column(name = "percentual_desconto", precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    // NOVO: Valor real descontado (Ex: R$ 5,00)
    @Column(name = "valor_desconto", precision = 15, scale = 2)
    private BigDecimal valorDesconto;

    // ATUALIZADO: Passa a ser o Valor Final (Líquido) pago pelo cliente
    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
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