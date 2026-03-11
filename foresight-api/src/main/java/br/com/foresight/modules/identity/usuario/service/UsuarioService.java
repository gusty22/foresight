package br.com.foresight.modules.identity.usuario.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.modules.identity.empresa.dto.EmpresaDto;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.dto.AtualizarPerfilRequest;
import br.com.foresight.modules.identity.usuario.dto.AtualizarSenhaRequest;
import br.com.foresight.modules.identity.usuario.dto.PerfilUsuarioDto;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import br.com.foresight.modules.identity.usuario.enums.Role;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IEmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    private Usuario getUsuarioLogadoSeguro() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RegraNegocioException("Acesso negado. Sessão inexistente ou expirada.");
        }

        if (auth.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }

        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RegraNegocioException("Segurança: Usuário logado não encontrado na base de dados."));
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioDto obterMeuPerfil() {
        Usuario usuario = getUsuarioLogadoSeguro();

        EmpresaDto empresaDto = null;
        Optional<Empresa> empresaOpt = empresaRepository.findByDonoId(usuario.getId());

        if (empresaOpt.isPresent()) {
            Empresa e = empresaOpt.get();
            empresaDto = new EmpresaDto(
                    e.getId(), e.getNome(), e.getCnpj(), e.getEmail(),
                    e.getTelefone(), e.getCep(), e.getLogradouro(),
                    e.getNumero(), e.getBairro(), e.getCidade(),
                    e.getEstado(), e.getProLaboreDesejado(), e.getTipo()
            );
        } else if (usuario.getRole() != Role.ROLE_SUPER_ADMIN) {
            throw new RegraNegocioException("Erro de integridade: Sua conta de cliente não possui uma empresa vinculada.");
        }

        return new PerfilUsuarioDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                empresaDto
        );
    }

    @Transactional
    public PerfilUsuarioDto atualizarMeuPerfil(AtualizarPerfilRequest request) {
        Usuario usuario = getUsuarioLogadoSeguro();

        usuario.setNome(request.nome());
        usuario.setTelefone(request.telefone());

        usuarioRepository.save(usuario);
        return obterMeuPerfil();
    }

    @Transactional
    public void atualizarMinhaSenha(AtualizarSenhaRequest request) {
        Usuario usuario = getUsuarioLogadoSeguro();

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual informada está incorreta.");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletarMinhaConta() {
        Usuario usuario = getUsuarioLogadoSeguro();

        if (usuario.getRole() == Role.ROLE_SUPER_ADMIN) {
            throw new RegraNegocioException("Operação negada: A conta de administração master não pode ser removida pelo painel.");
        }

        Optional<Empresa> empresaOpt = empresaRepository.findByDonoId(usuario.getId());
        empresaOpt.ifPresent(empresaRepository::delete);
        usuarioRepository.delete(usuario);
    }
}