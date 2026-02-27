package br.com.foresight.core.config;

import br.com.foresight.modules.identity.usuario.entity.Usuario;
import br.com.foresight.modules.identity.usuario.enums.Role;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeederConfig {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.superadmin.email:admin@foresight.com}")
    private String adminEmail;

    @Value("${app.security.superadmin.password:TrocarMudar123!}")
    private String adminPassword;

    @Value("${app.security.superadmin.name:Super Admin Foresight}")
    private String adminName;

    @Bean
    @Transactional
    public CommandLineRunner initSuperAdmin() {
        return args -> {
            log.info("Verificando a existência do Super Admin na base de dados...");

            boolean superAdminExists = usuarioRepository.existsByRole(Role.ROLE_SUPER_ADMIN);

            if (!superAdminExists) {
                log.warn("Nenhum Super Admin encontrado. Provisionando usuário root...");

                Usuario superAdmin = Usuario.builder()
                        .nome(adminName)
                        .email(adminEmail)
                        .senha(passwordEncoder.encode(adminPassword))
                        .role(Role.ROLE_SUPER_ADMIN)
                        .telefone("00000000000")
                        .build();

                usuarioRepository.save(superAdmin);

                log.info("Super Admin criado com sucesso. Email: {}", adminEmail);
                log.warn("ATENÇÃO: Caso esta seja uma senha padrão, troque-a imediatamente após o primeiro login.");
            } else {
                log.info("Super Admin já provisionado. Nenhuma ação necessária.");
            }
        };
    }
}