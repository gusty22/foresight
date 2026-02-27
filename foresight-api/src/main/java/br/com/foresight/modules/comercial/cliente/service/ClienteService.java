package br.com.foresight.modules.comercial.cliente.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.cliente.dto.ClienteDto;
import br.com.foresight.modules.comercial.cliente.dto.ClienteRequest;
import br.com.foresight.modules.comercial.cliente.entity.Cliente;
import br.com.foresight.modules.comercial.cliente.repository.IClienteRepository;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final IClienteRepository clienteRepository;
    private final IEmpresaRepository empresaRepository;

    // Utilitário de segurança: Pega o Tenant pelo Token JWT, impossível de ser burlado por URL
    private Empresa getEmpresaLogada() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Sessão inválida. Empresa não identificada.");
        }
        return empresaRepository.findById(tenantId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada."));
    }

    @Transactional
    public ClienteDto salvar(ClienteRequest request) {
        Cliente cliente = converterParaEntidade(request);
        cliente.setEmpresa(getEmpresaLogada()); // Atribui segurança forçada

        if (cliente.getStatusCliente() == null || cliente.getStatusCliente().isBlank()) {
            cliente.setStatusCliente("ATIVO");
        }

        return converterParaDto(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDto atualizar(Long id, ClienteRequest request) {
        // Se tentar invadir o ID de um cliente que pertence a outro tenant, o Hibernate retorna Vazio
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado ou você não tem permissão para editá-lo."));

        atualizarDados(cliente, request);
        return converterParaDto(clienteRepository.save(cliente));
    }

    public List<ClienteDto> listarPorEmpresa() {
        // O Hibernate Filter restringe esse findAll apenas para a empresa logada. Perfeitamente escalável.
        return clienteRepository.findAll().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    public List<ClienteDto> buscarClientesAutocomplete(String termo) {
        if (termo == null || termo.length() < 2) {
            return List.of(); // Evita queries pesadas se o usuário digitou só 1 letra
        }
        return clienteRepository.buscarPorTermoSeguro(termo).stream()
                .map(this::converterParaDto)
                .toList();
    }

    public ClienteDto buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .map(this::converterParaDto)
                .orElseThrow(() -> new RegraNegocioException("Cliente não encontrado."));
    }

    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RegraNegocioException("Cliente não encontrado.");
        }
        clienteRepository.deleteById(id);
    }

    private void atualizarDados(Cliente cliente, ClienteRequest req) {
        cliente.setNome(req.nome());
        cliente.setDocumento(req.documento());
        cliente.setTelefone(req.telefone());
        cliente.setTelefoneAlternativo(req.telefoneAlternativo());
        cliente.setEmail(req.email());
        cliente.setDataNascimento(req.dataNascimento());
        cliente.setCep(req.cep());
        cliente.setLogradouro(req.logradouro());
        cliente.setNumero(req.numero());
        cliente.setBairro(req.bairro());
        cliente.setCidade(req.cidade());
        cliente.setEstado(req.estado());
        cliente.setTipoCliente(req.tipoCliente());
        cliente.setInscricaoEstadual(req.inscricaoEstadual());
        cliente.setCondicoesEspeciais(req.condicoesEspeciais());
        cliente.setObservacoes(req.observacoes());

        if (req.statusCliente() != null && !req.statusCliente().isBlank()) {
            cliente.setStatusCliente(req.statusCliente());
        }
    }

    private Cliente converterParaEntidade(ClienteRequest req) {
        return Cliente.builder()
                .nome(req.nome())
                .documento(req.documento())
                .telefone(req.telefone())
                .telefoneAlternativo(req.telefoneAlternativo())
                .email(req.email())
                .dataNascimento(req.dataNascimento())
                .cep(req.cep())
                .logradouro(req.logradouro())
                .numero(req.numero())
                .bairro(req.bairro())
                .cidade(req.cidade())
                .estado(req.estado())
                .tipoCliente(req.tipoCliente())
                .inscricaoEstadual(req.inscricaoEstadual())
                .condicoesEspeciais(req.condicoesEspeciais())
                .observacoes(req.observacoes())
                .statusCliente(req.statusCliente())
                .build();
    }

    private ClienteDto converterParaDto(Cliente c) {
        return new ClienteDto(
                c.getId(),
                c.getNome(),
                c.getDocumento(),
                c.getTelefone(),
                c.getTelefoneAlternativo(),
                c.getEmail(),
                c.getDataNascimento(),
                c.getCep(),
                c.getLogradouro(),
                c.getNumero(),
                c.getBairro(),
                c.getCidade(),
                c.getEstado(),
                c.getTipoCliente(),
                c.getInscricaoEstadual(),
                c.getCondicoesEspeciais(),
                c.getObservacoes(),
                c.getStatusCliente()
        );
    }
}