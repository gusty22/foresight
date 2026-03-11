package br.com.foresight.modules.identity.empresa.entity;

import br.com.foresight.core.domain.BaseAuditEntity;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import br.com.foresight.modules.identity.empresa.model.TipoEmpresa;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "empresas")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario dono;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String cnpj;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 10)
    private String cep;

    @Column(length = 255)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEmpresa tipo;

    @Column(name = "prolabore_desejado", precision = 12, scale = 2)
    private BigDecimal proLaboreDesejado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmpresa status;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = StatusEmpresa.ATIVA;
        }
    }
}