package br.com.foresight.modules.financeiro.despesa.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.financeiro.despesa.dto.DespesaDto;
import br.com.foresight.modules.financeiro.despesa.dto.DespesaRequest;
import br.com.foresight.modules.financeiro.despesa.entity.Despesa;
import br.com.foresight.modules.financeiro.despesa.repository.IDespesaRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.FluxoCaixa;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DespesaService {

    private final IDespesaRepository despesaRepository;
    private final IFluxoCaixaRepository fluxoCaixaRepository;
    private final IEmpresaRepository empresaRepository;

    private Empresa getEmpresaSegura(Long tenantId) {
        return empresaRepository.findById(tenantId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada no contexto de segurança."));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public DespesaDto criar(DespesaRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RegraNegocioException("Acesso negado. Tenant não identificado.");

        Empresa empresa = getEmpresaSegura(tenantId);

        Despesa despesa = Despesa.builder()
                .descricao(request.descricao())
                .categoria(request.categoria())
                .valor(request.valor())
                .data(request.data())
                .tipo(request.tipo())
                .ehPessoal(request.ehPessoal())
                .status("PAGO")
                .build();

        despesa.setEmpresa(empresa);

        Despesa despesaSalva = despesaRepository.save(despesa);

        registrarSaidaFluxoCaixa(despesaSalva, empresa);

        return converterParaDto(despesaSalva);
    }

    @Transactional(readOnly = true)
    public List<DespesaDto> listarPorEmpresa() {
        Long tenantId = TenantContext.getCurrentTenant();
        return despesaRepository.findAllByEmpresaIdOrderByDataDesc(tenantId).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void excluir(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        Empresa empresa = getEmpresaSegura(tenantId);

        Despesa despesa = despesaRepository.findByIdAndEmpresaId(id, tenantId)
                .orElseThrow(() -> new RegraNegocioException("Despesa não encontrada ou não pertence a esta empresa."));

        estornarNoFluxoDeCaixa(despesa.getId(), empresa);

        despesaRepository.delete(despesa);
    }

    // ===================================================================================
    // MÉTODOS PRIVADOS DE INTEGRIDADE FINANCEIRA
    // ===================================================================================

    private void registrarSaidaFluxoCaixa(Despesa despesa, Empresa empresa) {
        BigDecimal saldoAtual = fluxoCaixaRepository.findTopByEmpresaIdOrderByDataHoraDesc(empresa.getId())
                .map(FluxoCaixa::getSaldoAposMovimentacao)
                .orElse(BigDecimal.ZERO);

        BigDecimal novoSaldo = saldoAtual.subtract(despesa.getValor());

        FluxoCaixa fluxo = FluxoCaixa.builder()
                .descricao("Despesa: " + despesa.getDescricao())
                .valor(despesa.getValor())
                .tipo(TipoMovimentacao.SAIDA)
                .dataHora(despesa.getData())
                .saldoAposMovimentacao(novoSaldo)
                .categoriaFluxo(despesa.isEhPessoal() ? CategoriaFluxo.PESSOAL : CategoriaFluxo.EMPRESA)
                .origem("DESPESA")
                .origemId(despesa.getId())
                .estornado(false)
                .build();
        fluxo.setEmpresa(empresa);

        fluxoCaixaRepository.save(fluxo);
    }

    private void estornarNoFluxoDeCaixa(Long despesaId, Empresa empresa) {
        FluxoCaixa fluxoOriginal = fluxoCaixaRepository.findByOrigemAndOrigemIdAndEmpresaId("DESPESA", despesaId, empresa.getId())
                .orElse(null);

        if (fluxoOriginal != null && !fluxoOriginal.isEstornado()) {
            fluxoOriginal.setEstornado(true);
            fluxoCaixaRepository.save(fluxoOriginal);

            BigDecimal saldoAtual = fluxoCaixaRepository.findTopByEmpresaIdOrderByDataHoraDesc(empresa.getId())
                    .map(FluxoCaixa::getSaldoAposMovimentacao)
                    .orElse(BigDecimal.ZERO);

            FluxoCaixa estorno = FluxoCaixa.builder()
                    .descricao("Estorno de Despesa: " + fluxoOriginal.getDescricao())
                    .valor(fluxoOriginal.getValor())
                    .tipo(TipoMovimentacao.ENTRADA)
                    .dataHora(java.time.LocalDateTime.now())
                    .saldoAposMovimentacao(saldoAtual.add(fluxoOriginal.getValor()))
                    .categoriaFluxo(fluxoOriginal.getCategoriaFluxo())
                    .origem("ESTORNO_DESPESA")
                    .origemId(fluxoOriginal.getId())
                    .referenciaEstornoId(fluxoOriginal.getId())
                    .estornado(false)
                    .build();
            estorno.setEmpresa(empresa);

            fluxoCaixaRepository.save(estorno);
        }
    }

    private DespesaDto converterParaDto(Despesa despesa) {
        // CORREÇÃO: Construtor completo e alinhado com o DespesaDto
        return new DespesaDto(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getCategoria(),
                despesa.getValor(),
                despesa.getData(),
                despesa.getTipo(),
                despesa.isEhPessoal(),
                despesa.getStatus()
        );
    }
}