package br.com.foresight.modules.identity.auth;

public record TokenResponse(
        String token,
        String nomeUsuario,
        String nomeEmpresa
) {}