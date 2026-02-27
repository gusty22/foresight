package br.com.foresight.core.security;

import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug-auth")
public class DebugAuthController {

    @GetMapping("/inspect")
    public Map<String, Object> inspect() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> report = new HashMap<>();

        if (auth == null) {
            report.put("status", "ERRO: SecurityContext está VAZIO");
            return report;
        }

        report.put("principal_type", auth.getPrincipal().getClass().getName());
        report.put("authorities", auth.getAuthorities());
        report.put("tenant_context_id", TenantContext.getCurrentTenant());

        if (auth.getPrincipal() instanceof Usuario user) {
            report.put("user_email", user.getEmail());
            report.put("user_role", user.getRole());
            report.put("is_super_admin", user.getRole().name().equals("ROLE_SUPER_ADMIN"));
        }

        return report;
    }
}