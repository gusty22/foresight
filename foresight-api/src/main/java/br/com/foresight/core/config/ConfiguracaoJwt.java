package br.com.foresight.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoJwt {

    @Value("${api.security.token.secret}")
    private String segredo;

    // Você pode adicionar métodos aqui para retornar o algoritmo padrão
    // ou configurações de expiração customizadas.

    public String getSegredo() {
        return segredo;
    }
}