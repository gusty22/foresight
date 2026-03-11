package br.com.foresight.core.security;

import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private final ProvedorJwtToken tokenService;
    private final IUsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarToken(request);

        if (token != null) {
            DecodedJWT decodedJWT = tokenService.decodificarToken(token);

            if (decodedJWT != null) {
                String email = decodedJWT.getSubject();
                var tenantClaim = decodedJWT.getClaim("tenantId");
                var roleClaim = decodedJWT.getClaim("role");

                if (!tenantClaim.isNull()) {
                    TenantContext.setCurrentTenant(tenantClaim.asLong());
                }

                repository.findByEmail(email).ifPresent(usuario -> {
                    String roleName = roleClaim.isNull() ? usuario.getRole().name() : roleClaim.asString();

                    if (!roleName.startsWith("ROLE_")) roleName = "ROLE_" + roleName;

                    var authorities = List.of(new SimpleGrantedAuthority(roleName));
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String recuperarToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer ", "");
    }
}