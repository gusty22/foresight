package br.com.foresight.modules.comercial.apoio.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "categorias_produto")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProduto extends BaseTenantEntity {

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "cor_hexadecimal", length = 7)
    @Builder.Default
    private String corHexadecimal = "#CCCCCC";
}