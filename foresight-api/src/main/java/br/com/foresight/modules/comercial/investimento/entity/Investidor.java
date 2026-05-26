package br.com.foresight.modules.comercial.investimento.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "investidores")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investidor extends BaseTenantEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(name = "chave_pix", length = 150)
    private String chavePix;

    @Column(length = 20)
    @Builder.Default
    private String status = "ATIVO";

}