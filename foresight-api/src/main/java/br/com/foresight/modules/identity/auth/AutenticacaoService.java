package br.com.foresight.modules.identity.auth;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.security.ProvedorJwtToken;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import br.com.foresight.modules.identity.usuario.enums.Role;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutenticacaoService {

    private final IUsuarioRepository usuarioRepository;
    private final IEmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProvedorJwtToken tokenService;

    @Transactional
    public TokenResponse registrar(RegistroRequest dados) {
        if (usuarioRepository.existsByEmail(dados.email())) {
            throw new RegraNegocioException("Este e-mail já está em uso.");
        }

        Usuario usuario = Usuario.builder()
                .nome(dados.nomeUsuario())
                .email(dados.email())
                .senha(passwordEncoder.encode(dados.senha()))
                .telefone(dados.telefoneUsuario())
                .role(Role.ROLE_TENANT_ADMIN)
                .build();
        usuario = usuarioRepository.save(usuario);

        Empresa empresa = Empresa.builder()
                .dono(usuario)
                .nome(dados.nomeEmpresa())
                .cnpj(dados.cnpjEmpresa())
                .tipo(dados.tipoEmpresa())
                .status(br.com.foresight.modules.identity.empresa.enums.StatusEmpresa.ATIVA)
                .build();
        empresa = empresaRepository.save(empresa);

        String token = tokenService.gerarToken(usuario, empresa.getId());
        return new TokenResponse(token, usuario.getNome(), empresa.getNome());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.email())
                .orElseThrow(() -> new RegraNegocioException("Usuário ou senha inválidos."));

        if (!passwordEncoder.matches(dados.senha(), usuario.getSenha())) {
            throw new RegraNegocioException("Usuário ou senha inválidos.");
        }

        // LÓGICA DE MULTI-TENANCY PARA SUPER ADMIN
        Optional<Empresa> empresaOpt = empresaRepository.findByDonoId(usuario.getId());
        Long empresaId = null;
        String nomeExibicaoEmpresa = "Administração Global";

        // Se NÃO for Super Admin, a empresa é obrigatória para manter a integridade do SaaS
        if (!usuario.getRole().equals(Role.ROLE_SUPER_ADMIN)) {
            Empresa empresa = empresaOpt.orElseThrow(() ->
                    new RegraNegocioException("Erro de integridade: Usuário não possui uma empresa vinculada."));

            empresaId = empresa.getId();
            nomeExibicaoEmpresa = empresa.getNome();
        } else {
            log.info("Acesso administrativo master detectado: {}", usuario.getEmail());
        }

        String token = tokenService.gerarToken(usuario, empresaId);

        return new TokenResponse(token, usuario.getNome(), nomeExibicaoEmpresa);
    }
}