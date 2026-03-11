package br.com.foresight.modules.financeiro.despesa.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "despesas")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Despesa extends BaseTenantEntity {

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoDespesa tipo;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "PAGO";

    @Builder.Default
    @Column(name = "eh_pessoal")
    private boolean ehPessoal = false;
}