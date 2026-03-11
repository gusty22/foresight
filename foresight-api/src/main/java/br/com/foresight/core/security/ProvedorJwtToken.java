package br.com.foresight.core.security;

import br.com.foresight.modules.identity.usuario.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class ProvedorJwtToken {

    @Value("${api.security.token.secret}")
    private String segredo;

    private static final String ISSUER = "foresight-api";

    public String gerarToken(Usuario usuario, Long empresaId) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(segredo);
            var builder = JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("role", usuario.getRole().name())
                    .withExpiresAt(LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00")));

            if (empresaId != null) {
                builder.withClaim("tenantId", empresaId);
            }

            return builder.sign(algoritmo);
        } catch (Exception e) {
            throw new RuntimeException("Erro fatal na geração do token", e);
        }
    }

    public DecodedJWT decodificarToken(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(segredo);
            return JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (Exception e) {
            return null;
        }
    }
}