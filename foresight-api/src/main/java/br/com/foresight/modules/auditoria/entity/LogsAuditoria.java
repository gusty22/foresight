package br.com.foresight.modules.auditoria.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogsAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificação FLAT do Tenant (Sem JOIN para garantir Insert de ultra-alta performance)
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, length = 150, updatable = false)
    private String entidadeNome;

    @Column(name = "entidade_id", updatable = false)
    private Long entidadeId;

    @Column(nullable = false, length = 50, updatable = false)
    private String acao; // CRIACAO, EDICAO, DELECAO, ESTORNO

    @Column(columnDefinition = "TEXT", updatable = false)
    private String detalhes;

    @Column(nullable = false, length = 150, updatable = false)
    private String usuarioEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;
}