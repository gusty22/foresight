package br.com.foresight.modules.comercial.cliente.entity;

import br.com.foresight.core.domain.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente extends BaseTenantEntity {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String documento;

    @Column(length = 20)
    private String telefone;

    @Column(name = "telefone_alternativo", length = 20)
    private String telefoneAlternativo;

    @Column(length = 255)
    private String email;

    @Column(name = "data_nascimento_fundacao")
    private LocalDate dataNascimento;

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

    @Column(name = "tipo_cliente", length = 10)
    private String tipoCliente;

    @Column(name = "inscricao_estadual", length = 50)
    private String inscricaoEstadual;

    @Column(name = "condicoes_especiais", columnDefinition = "TEXT")
    private String condicoesEspeciais;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "status_cliente", length = 20)
    private String statusCliente;
}