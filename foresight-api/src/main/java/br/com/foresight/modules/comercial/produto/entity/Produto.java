package br.com.foresight.modules.comercial.produto.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "produtos")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produto extends BaseTenantEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "preco_custo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCusto;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "estoque_atual", nullable = false)
    @Builder.Default
    private Integer estoqueAtual = 0;

    @Column(name = "estoque_minimo", nullable = false)
    @Builder.Default
    private Integer estoqueMinimo = 5;

    @Version // MÁGICA ENTERPRISE: Previne Race Condition na atualização de estoque/preço
    private Long version;

    public BigDecimal getLucroUnidade() {
        if (precoVenda == null || precoCusto == null) return BigDecimal.ZERO;
        return precoVenda.subtract(precoCusto);
    }

    public BigDecimal calcularMargem() {
        if (precoVenda == null || precoVenda.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal lucro = getLucroUnidade();
        return lucro.divide(precoVenda, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}