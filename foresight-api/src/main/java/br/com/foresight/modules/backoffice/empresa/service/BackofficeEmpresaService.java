package br.com.foresight.modules.backoffice.empresa.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.modules.backoffice.empresa.dto.AlterarStatusEmpresaRequest;
import br.com.foresight.modules.backoffice.empresa.dto.EmpresaGlobalDto;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackofficeEmpresaService {

    private final IEmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<EmpresaGlobalDto> listarTodasEmpresas() {
        List<Empresa> empresas = empresaRepository.findAll();

        return empresas.stream()
                .map(e -> new EmpresaGlobalDto(
                        e.getId(),
                        e.getNome(), // Ajustado de razaoSocial para nome conforme sua entidade Empresa
                        e.getCnpj(),
                        e.getStatus(),
                        e.getCriadoEm() // Assumindo que getCriadoEm() já devolve um LocalDateTime do BaseAuditEntity
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public EmpresaGlobalDto alterarStatus(Long empresaId, AlterarStatusEmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada na base global."));

        // Proteção Nível Deus: Evita auto-bloqueio do Super Admin
        if (empresa.getId().equals(1L) && request.novoStatus() != br.com.foresight.modules.identity.empresa.enums.StatusEmpresa.ATIVA) {
            throw new RegraNegocioException("Ação Negada: A empresa administradora não pode ser bloqueada.");
        }

        empresa.setStatus(request.novoStatus());
        empresaRepository.save(empresa);

        return new EmpresaGlobalDto(
                empresa.getId(),
                empresa.getNome(),
                empresa.getCnpj(),
                empresa.getStatus(),
                empresa.getCriadoEm()
        );
    }
}