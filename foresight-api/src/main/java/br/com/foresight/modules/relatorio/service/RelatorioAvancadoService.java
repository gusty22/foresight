package br.com.foresight.modules.relatorio.service;

import br.com.foresight.core.exception.RegraNegocioException;
import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.FluxoCaixa;
import br.com.foresight.modules.financeiro.fluxo_caixa.repository.IFluxoCaixaRepository;
import br.com.foresight.modules.relatorio.dto.TransacaoRelatorioDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioAvancadoService {

    private final IFluxoCaixaRepository fluxoCaixaRepository;

    @Transactional(readOnly = true)
    public Page<TransacaoRelatorioDto> buscarDados(
            String termo, LocalDate dataInicio, LocalDate dataFim, String tipo, String categoria, Pageable pageable) {

        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) throw new RegraNegocioException("Sessão inválida.");

        Specification<FluxoCaixa> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Blindagem Multi-Tenant OBRIGATÓRIA
            predicates.add(cb.equal(root.get("empresa").get("id"), tenantId));

            // 2. Filtros Dinâmicos
            if (termo != null && !termo.isBlank()) {
                String likePattern = "%" + termo.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("descricao")), likePattern));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), dataInicio.atStartOfDay()));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), dataFim.atTime(LocalTime.MAX)));
            }
            if (tipo != null && !tipo.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("tipo").as(String.class)), tipo.toUpperCase()));
            }
            if (categoria != null && !categoria.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("categoriaFluxo").as(String.class)), categoria.toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return fluxoCaixaRepository.findAll(spec, pageable)
                .map(f -> new TransacaoRelatorioDto(
                        f.getId(), f.getDataHora(), f.getDescricao(), "---",
                        f.getCategoriaFluxo() != null ? f.getCategoriaFluxo().name() : "GERAL",
                        f.getValor(), f.getTipo().name(), "CONSOLIDADO"
                ));
    }

    // Nota: O método de exportar para PDF/Excel seria implementado aqui.
    // Para simplificar esta entrega, deixaremos o esqueleto pronto.
    public byte[] exportarParaPdf(String termo, LocalDate dataInicio, LocalDate dataFim, String tipo, String categoria) {
        // ... Lógica de geração de arquivo em memória usando iText ou Apache POI
        return new byte[0]; // Retorno mockado
    }
}