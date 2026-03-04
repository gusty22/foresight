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

    @Column(name = "empresa_id", updatable = false)
    private Long empresaId; // Pode ser nulo em ações globais do Super Admin

    @Column(nullable = false, length = 150, updatable = false)
    private String entidadeNome;

    @Column(name = "entidade_id", updatable = false)
    private Long entidadeId;

    @Column(nullable = false, length = 50, updatable = false)
    private String acao;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String detalhes;

    @Column(nullable = false, length = 150, updatable = false)
    private String usuarioEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;
}