package br.com.foresight.modules.identity.empresa.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.modules.identity.empresa.dto.EmpresaDto;
import br.com.foresight.modules.identity.empresa.dto.EmpresaRequest;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final IEmpresaRepository repository;

    private Usuario getUsuarioLogadoSeguro() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new RegraNegocioException("Falha de segurança: Usuário não autenticado ou token inválido.");
    }

    private Empresa buscarEmpresaDoUsuario(Long id, Long usuarioId) {
        return repository.findByIdAndDonoId(id, usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Acesso Negado: Empresa não encontrada ou não pertence à sua conta."));
    }

    @Transactional
    public EmpresaDto criar(EmpresaRequest request) {
        Usuario usuarioLogado = getUsuarioLogadoSeguro();

        if (repository.findByDonoId(usuarioLogado.getId()).isPresent()) {
            throw new RegraNegocioException("Operação negada: Você já possui uma empresa cadastrada.");
        }

        Empresa empresa = Empresa.builder()
                .nome(request.nome())
                .cnpj(request.cnpj())
                .email(request.email())
                .telefone(request.telefone())
                .cep(request.cep())
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .estado(request.estado())
                .tipo(request.tipo())
                .proLaboreDesejado(request.proLaboreDesejado())
                .dono(usuarioLogado)
                .build();

        return converterParaDto(repository.save(empresa));
    }

    @Transactional(readOnly = true)
    public List<EmpresaDto> listarMinhasEmpresas() {
        Usuario usuarioLogado = getUsuarioLogadoSeguro();
        return repository.findByDonoId(usuarioLogado.getId())
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaDto buscarPorId(Long id) {
        Usuario usuarioLogado = getUsuarioLogadoSeguro();
        Empresa empresa = buscarEmpresaDoUsuario(id, usuarioLogado.getId());
        return converterParaDto(empresa);
    }

    @Transactional
    public EmpresaDto atualizar(Long id, EmpresaRequest request) {
        Usuario usuarioLogado = getUsuarioLogadoSeguro();
        Empresa empresa = buscarEmpresaDoUsuario(id, usuarioLogado.getId());

        empresa.setNome(request.nome());
        empresa.setCnpj(request.cnpj());
        empresa.setEmail(request.email());
        empresa.setTelefone(request.telefone());
        empresa.setCep(request.cep());
        empresa.setLogradouro(request.logradouro());
        empresa.setNumero(request.numero());
        empresa.setBairro(request.bairro());
        empresa.setCidade(request.cidade());
        empresa.setEstado(request.estado());
        empresa.setTipo(request.tipo());
        empresa.setProLaboreDesejado(request.proLaboreDesejado());

        return converterParaDto(repository.save(empresa));
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuarioLogado = getUsuarioLogadoSeguro();
        Empresa empresa = buscarEmpresaDoUsuario(id, usuarioLogado.getId());
        repository.delete(empresa);
    }

    private EmpresaDto converterParaDto(Empresa e) {
        return new EmpresaDto(
                e.getId(), e.getNome(), e.getCnpj(), e.getEmail(),
                e.getTelefone(), e.getCep(), e.getLogradouro(),
                e.getNumero(), e.getBairro(), e.getCidade(),
                e.getEstado(), e.getProLaboreDesejado(), e.getTipo()
        );
    }
}