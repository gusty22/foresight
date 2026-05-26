package br.com.foresight.modules.comercial.produto.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import br.com.foresight.modules.comercial.apoio.entity.CategoriaProduto;
import br.com.foresight.modules.comercial.apoio.entity.Fornecedor;
import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import jakarta.persistence.*;
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

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaProduto categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

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

    // NOVOS CAMPOS PARA RELACIONAMENTO COM INVESTIDOR (via lote)
    // Não armazenamos diretamente no produto, mas sim no lote inicial.
    // Para facilitar consultas, podemos adicionar campos transient ou buscar via repositório.
    // O campo abaixo é apenas para referência (não persistido)
    @Transient
    private Long investidorId;

    @Transient
    private String investidorNome;

    @Transient
    private BigDecimal percentualLucroInvestidor;

    @Version
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