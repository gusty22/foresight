package br.com.foresight.modules.comercial.investimento.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import br.com.foresight.modules.comercial.produto.entity.Produto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_estoque")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteEstoque extends BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // Se for nulo, o lote pertence à própria empresa (não tem divisão de lucro)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investidor_id")
    private Investidor investidor;

    @Column(name = "quantidade_inicial", nullable = false)
    private Integer quantidadeInicial;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel;

    @Column(name = "custo_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoUnitario;

    @Column(name = "percentual_lucro_investidor", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentualLucroInvestidor = BigDecimal.ZERO;

    @Column(name = "data_entrada")
    @Builder.Default
    private LocalDateTime dataEntrada = LocalDateTime.now();

    @Column(length = 20)
    @Builder.Default
    private String status = "ABERTO";

    @Version
    private Long version; // Fundamental para Lock Otimista (Evitar concorrência)
}