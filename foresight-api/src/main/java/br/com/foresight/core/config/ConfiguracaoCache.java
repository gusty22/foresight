package br.com.foresight.core.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // Ativa o suporte a cache no Spring
public class ConfiguracaoCache {

    @Bean
    public CacheManager cacheManager() {
        // Gerenciador de cache simples em memória
        return new ConcurrentMapCacheManager("dashboard", "produtos", "empresas");
    }
}