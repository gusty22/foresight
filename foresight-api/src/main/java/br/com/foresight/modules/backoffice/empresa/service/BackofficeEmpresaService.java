package br.com.foresight.modules.backoffice.empresa.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.modules.backoffice.empresa.dto.AlterarStatusEmpresaRequest;
import br.com.foresight.modules.backoffice.empresa.dto.EmpresaGlobalDto;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackofficeEmpresaService {

    private final IEmpresaRepository empresaRepository;
    private final IUsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<EmpresaGlobalDto> listarEmpresasPaginado(String termo, Pageable pageable) {
        Page<Empresa> pagina;

        if (termo != null && !termo.isBlank()) {
            pagina = empresaRepository.buscarPorTermoPaginado(termo.toLowerCase(), pageable);
        } else {
            pagina = empresaRepository.findAll(pageable);
        }

        return pagina.map(this::converterParaDto);
    }

    @Transactional
    public EmpresaGlobalDto alterarStatus(Long empresaId, AlterarStatusEmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada na base global."));

        if (empresa.getId().equals(1L) && request.novoStatus() != StatusEmpresa.ATIVA) {
            log.warn("Tentativa de bloqueio da Empresa Master (ID 1) interceptada e negada.");
            throw new RegraNegocioException("Ação Negada: A empresa administradora master não pode ser suspensa.");
        }

        StatusEmpresa statusAntigo = empresa.getStatus();
        empresa.setStatus(request.novoStatus());
        empresaRepository.save(empresa);

        log.warn("AUDITORIA: Status da empresa '{}' (ID {}) alterado de {} para {}. Motivo: {}",
                empresa.getNome(), empresa.getId(), statusAntigo, request.novoStatus(), request.motivo());

        return converterParaDto(empresa);
    }

    @Transactional(readOnly = true)
    public Map<String, String> gerarTokenImpersonation(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada."));

        log.warn("AUDITORIA SEVERA: Um Super Admin solicitou acesso de suporte (Impersonation) para a empresa '{}' (ID {}).",
                empresa.getNome(), empresa.getId());
        return Map.of("token", "mock-jwt-impersonation-token-for-tenant-" + empresaId);
    }

    private EmpresaGlobalDto converterParaDto(Empresa e) {
        long usuariosAtivos = 1L;

        return new EmpresaGlobalDto(
                e.getId(),
                e.getNome(),
                e.getCnpj(),
                e.getStatus(),
                "PRO", // Mock
                e.getCriadoEm(),
                usuariosAtivos
        );
    }
}