package br.com.foresight.core.config;

import br.com.foresight.core.tenant.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@RequiredArgsConstructor
/* * MÁGICA ENTERPRISE: VIA_DTO garante que a paginação enviada ao Angular
 * siga um padrão JSON estável e compatível com DTOs, eliminando o WARN
 * do PageModule e protegendo o front-end contra mudanças internas do Spring.
 */
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Blindagem do Tenant: O interceptor só atua em rotas de negócio (/api/sistema/...)
        // Rotas de Auth e Backoffice (Master) são ignoradas para evitar conflitos de contexto.
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**")
                .excludePathPatterns("/api/backoffice/**")
                .excludePathPatterns("/api/public/**"); // Sugestão: adicione se houver rotas abertas
    }
}