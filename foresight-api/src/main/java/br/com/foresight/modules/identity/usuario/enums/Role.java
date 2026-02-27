package br.com.foresight.modules.identity.usuario.enums;

public enum Role {
    ROLE_TENANT_ADMIN, // Dono da empresa assinante
    ROLE_TENANT_USER,  // Funcionário comum
    ROLE_SUPER_ADMIN   // Dono da plataforma (Você)
}