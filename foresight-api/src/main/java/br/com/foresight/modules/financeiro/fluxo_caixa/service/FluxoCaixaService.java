package br.com.foresight.modules.financeiro.fluxo_caixa.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.financeiro.fluxo_caixa.dto.FluxoCaixaDto;
import br.com.foresight.modules.financeiro.fluxo_caixa.dto.FluxoCaixaRequest;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.FluxoCaixa;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FluxoCaixaService {

    private final IFluxoCaixaRepository repository;
    private final IEmpresaRepository empresaRepository;

    private Long getTenantIdSeguro() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RegraNegocioException("Violação de Segurança: Sessão de tenant inexistente.");
        }
        return tenantId;
    }


    private Empresa lockEmpresa(Long tenantId) {
        return empresaRepository.findByIdForUpdate(tenantId)
                .orElseThrow(() -> new RegraNegocioException("Empresa não encontrada ou falha no lock transacional."));
    }

    @Transactional
    public FluxoCaixaDto registrarMovimentacao(FluxoCaixaRequest request) {
        Long tenantId = getTenantIdSeguro();
        Empresa empresa = lockEmpresa(tenantId);

        BigDecimal saldoAnterior = repository.findTopByEmpresaIdOrderByDataHoraDesc(tenantId)
                .map(FluxoCaixa::getSaldoAposMovimentacao)
                .orElse(BigDecimal.ZERO);

        BigDecimal valorLancamento = request.tipo() == TipoMovimentacao.SAIDA
                ? request.valor().abs().negate()
                : request.valor().abs();

        FluxoCaixa entidade = FluxoCaixa.builder()
                .descricao(request.descricao())
                .valor(valorLancamento)
                .tipo(request.tipo())
                .categoriaFluxo(request.categoriaFluxo())
                .dataHora(LocalDateTime.now())
                .saldoAposMovimentacao(saldoAnterior.add(valorLancamento))
                .estornado(false)
                .build();

        entidade.setEmpresa(empresa);
        return toDto(repository.save(entidade));
    }

    @Transactional
    public void registrarMovimentacaoInterna(Empresa empresa, String descricao, BigDecimal valor, TipoMovimentacao tipo, CategoriaFluxo categoria) {
        FluxoCaixaRequest req = new FluxoCaixaRequest(descricao, valor, tipo, categoria);
        registrarMovimentacao(req);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixaDto> listarHistorico() {
        return repository.findByEmpresaIdOrderByDataHoraDesc(getTenantIdSeguro())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public FluxoCaixaDto buscarPorId(Long id) {
        Long tenantId = getTenantIdSeguro();
        FluxoCaixa fc = repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada."));

        if (!fc.getEmpresa().getId().equals(tenantId)) {
            throw new RegraNegocioException("Acesso Negado: Esta movimentação pertence a outra entidade.");
        }
        return toDto(fc);
    }

    @Transactional
    public FluxoCaixaDto estornarMovimentacao(Long id) {
        Long tenantId = getTenantIdSeguro();
        Empresa empresa = lockEmpresa(tenantId);

        FluxoCaixa original = repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada."));

        if (!original.getEmpresa().getId().equals(tenantId)) {
            throw new RegraNegocioException("Acesso Negado.");
        }

        if (original.isEstornado()) {
            throw new RegraNegocioException("Este lançamento já foi estornado anteriormente.");
        }

        original.setEstornado(true);
        repository.save(original);

        TipoMovimentacao tipoInverso = original.getTipo() == TipoMovimentacao.ENTRADA ? TipoMovimentacao.SAIDA : TipoMovimentacao.ENTRADA;

        BigDecimal saldoAnterior = repository.findTopByEmpresaIdOrderByDataHoraDesc(tenantId)
                .map(FluxoCaixa::getSaldoAposMovimentacao)
                .orElse(BigDecimal.ZERO);

        BigDecimal valorLancamento = tipoInverso == TipoMovimentacao.SAIDA
                ? original.getValor().abs().negate()
                : original.getValor().abs();

        FluxoCaixa estorno = FluxoCaixa.builder()
                .descricao("ESTORNO: " + original.getDescricao())
                .valor(valorLancamento)
                .tipo(tipoInverso)
                .categoriaFluxo(original.getCategoriaFluxo())
                .dataHora(LocalDateTime.now())
                .saldoAposMovimentacao(saldoAnterior.add(valorLancamento))
                .estornado(true)
                .referenciaEstornoId(original.getId())
                .build();

        estorno.setEmpresa(empresa);
        return toDto(repository.save(estorno));
    }

    public Map<String, BigDecimal> obterResumoFinanceiro() {
        Long tenantId = getTenantIdSeguro();
        BigDecimal saldoEmpresa = repository.somarPorCategoriaSeguro(tenantId, CategoriaFluxo.EMPRESA);
        BigDecimal saldoPessoal = repository.somarPorCategoriaSeguro(tenantId, CategoriaFluxo.PESSOAL);

        return Map.of(
                "saldoEmpresa", saldoEmpresa,
                "retiradaPessoal", saldoPessoal.abs()
        );
    }

    private FluxoCaixaDto toDto(FluxoCaixa f) {
        return new FluxoCaixaDto(
                f.getId(), f.getDescricao(), f.getValor(),
                f.getTipo(), f.getSaldoAposMovimentacao(),
                f.getDataHora(), f.getCategoriaFluxo(),
                f.isEstornado(), f.getReferenciaEstornoId()
        );
    }
}