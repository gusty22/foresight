package br.com.foresight.modules.comercial.investimento.service;

import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.investimento.dto.InvestidorDto;
import br.com.foresight.modules.comercial.investimento.dto.LoteEstoqueDto;
import br.com.foresight.modules.comercial.investimento.dto.RelatorioInvestidorDto;
import br.com.foresight.modules.comercial.investimento.dto.RepasseInvestidorDto;
import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import br.com.foresight.modules.comercial.investimento.repository.IInvestidorRepository;
import br.com.foresight.modules.comercial.investimento.repository.ILoteEstoqueRepository;
import br.com.foresight.modules.comercial.investimento.repository.IRepasseInvestidorRepository;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.CategoriaFluxo;
import br.com.foresight.modules.financeiro.fluxo_caixa.entity.TipoMovimentacao;
import br.com.foresight.modules.financeiro.fluxo_caixa.service.FluxoCaixaService;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestimentoService {

    private final IInvestidorRepository investidorRepository;
    private final IRepasseInvestidorRepository repasseRepository;
    private final IEmpresaRepository empresaRepository;
    private final ILoteEstoqueRepository loteRepository;

    private final FluxoCaixaService fluxoCaixaService;

    // --- CRUD DE INVESTIDORES ---

    @Transactional(readOnly = true)
    public List<InvestidorDto> listarInvestidores() {
        return investidorRepository.findAllByEmpresaId(TenantContext.getCurrentTenant()).stream()
                .map(i -> new InvestidorDto(i.getId(), i.getNome(), i.getTelefone(), i.getChavePix(), i.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Investidor> buscarPorId(Long id) {
        return investidorRepository.findById(id)
                .filter(i -> i.getEmpresa() != null && i.getEmpresa().getId().equals(TenantContext.getCurrentTenant()));
    }

    @Transactional
    public Investidor salvarInvestidor(Investidor investidor) {
        var empresa = empresaRepository.getReferenceById(TenantContext.getCurrentTenant());
        investidor.setEmpresa(empresa);
        return investidorRepository.save(investidor);
    }

    @Transactional
    public Investidor atualizarInvestidor(Long id, InvestidorDto dto) {
        Investidor investidor = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Investidor não encontrado"));

        investidor.setNome(dto.nome());
        investidor.setTelefone(dto.telefone());
        investidor.setChavePix(dto.chavePix());
        investidor.setStatus(dto.status());

        return investidorRepository.save(investidor);
    }

    @Transactional
    public void inativarInvestidor(Long id) {
        Investidor investidor = buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Investidor não encontrado"));

        investidor.setStatus("INATIVO");
        investidorRepository.save(investidor);
    }

    // --- REPASSES E RELATÓRIOS ---

    @Transactional(readOnly = true)
    public List<RepasseInvestidorDto> listarRepassesPendentes() {
        return repasseRepository.findByEmpresaIdAndStatus(TenantContext.getCurrentTenant(), "PENDENTE").stream()
                .map(r -> new RepasseInvestidorDto(
                        r.getId(),
                        r.getInvestidor().getId(),
                        r.getInvestidor().getNome(),
                        r.getVenda().getId(),
                        r.getItemVenda().getProduto().getNome(),
                        r.getValorLucroTotal(),
                        r.getValorRepasse(),
                        r.getStatus(),
                        r.getCriadoEm(),
                        r.getDataPagamento()
                )).toList();
    }

    @Transactional(readOnly = true)
    public RelatorioInvestidorDto gerarRelatorioInvestidor(Long investidorId) {
        Investidor investidor = buscarPorId(investidorId)
                .orElseThrow(() -> new RuntimeException("Investidor não encontrado"));

        InvestidorDto invDto = new InvestidorDto(investidor.getId(), investidor.getNome(), investidor.getTelefone(), investidor.getChavePix(), investidor.getStatus());

        List<br.com.foresight.modules.comercial.investimento.entity.LoteEstoque> lotes = loteRepository.findByInvestidorId(investidorId);
        List<br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor> repasses = repasseRepository.findByInvestidorId(investidorId);

        BigDecimal totalInvestido = lotes.stream()
                .map(l -> l.getCustoUnitario().multiply(BigDecimal.valueOf(l.getQuantidadeInicial())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLucro = repasses.stream()
                .map(br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor::getValorLucroTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendente = repasses.stream()
                .filter(r -> "PENDENTE".equals(r.getStatus()))
                .map(br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor::getValorRepasse)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pago = repasses.stream()
                .filter(r -> "PAGO".equals(r.getStatus()))
                .map(br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor::getValorRepasse)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // CORREÇÃO APLICADA AQUI: 9 argumentos enviados para o LoteEstoqueDto
        List<LoteEstoqueDto> lotesDto = lotes.stream()
                .map(l -> new LoteEstoqueDto(
                        l.getId(),
                        l.getProduto().getNome(),
                        l.getInvestidor().getNome(),
                        l.getQuantidadeInicial(),
                        l.getQuantidadeDisponivel(),
                        l.getCustoUnitario(),
                        l.getPercentualLucroInvestidor(),
                        l.getStatus(),
                        l.getDataEntrada()
                ))
                .toList();

        List<RepasseInvestidorDto> repassesDto = repasses.stream()
                .map(r -> new RepasseInvestidorDto(
                        r.getId(),
                        r.getInvestidor().getId(),
                        r.getInvestidor().getNome(),
                        r.getVenda().getId(),
                        r.getItemVenda().getProduto().getNome(),
                        r.getValorLucroTotal(),
                        r.getValorRepasse(),
                        r.getStatus(),
                        r.getCriadoEm(),
                        r.getDataPagamento()
                ))
                .toList();

        return new RelatorioInvestidorDto(invDto, totalInvestido, totalLucro, pendente, pago, lotesDto, repassesDto);
    }

    @Transactional
    public void pagarInvestidor(Long investidorId, BigDecimal valorPagamento) {
        Investidor investidor = buscarPorId(investidorId)
                .orElseThrow(() -> new RuntimeException("Investidor não encontrado"));

        List<br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor> pendentes =
                repasseRepository.findByInvestidorIdAndStatusOrderByCriadoEmAsc(investidorId, "PENDENTE");

        BigDecimal saldoRestanteDoPagamento = valorPagamento;

        for (br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor repasse : pendentes) {
            if (saldoRestanteDoPagamento.compareTo(BigDecimal.ZERO) <= 0) break;

            if (repasse.getValorRepasse().compareTo(saldoRestanteDoPagamento) <= 0) {
                saldoRestanteDoPagamento = saldoRestanteDoPagamento.subtract(repasse.getValorRepasse());
                repasse.setStatus("PAGO");
                repasse.setDataPagamento(LocalDateTime.now());
                repasseRepository.save(repasse);
            } else {
                BigDecimal valorQueVaiFicarPendente = repasse.getValorRepasse().subtract(saldoRestanteDoPagamento);

                repasse.setValorRepasse(saldoRestanteDoPagamento);
                repasse.setStatus("PAGO");
                repasse.setDataPagamento(LocalDateTime.now());
                repasseRepository.save(repasse);

                br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor novoPendente =
                        br.com.foresight.modules.comercial.investimento.entity.RepasseInvestidor.builder()
                                .investidor(repasse.getInvestidor())
                                .venda(repasse.getVenda())
                                .itemVenda(repasse.getItemVenda())
                                .valorLucroTotal(repasse.getValorLucroTotal())
                                .valorRepasse(valorQueVaiFicarPendente)
                                .status("PENDENTE")
                                .build();
                novoPendente.setEmpresa(repasse.getEmpresa());
                repasseRepository.save(novoPendente);

                saldoRestanteDoPagamento = BigDecimal.ZERO;
            }
        }

        if (saldoRestanteDoPagamento.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("O valor informado (R$ " + valorPagamento + ") é maior que o total pendente.");
        }

        fluxoCaixaService.registrarMovimentacaoInterna(
                investidor.getEmpresa(),
                "Pagamento de Lucros - Sócio: " + investidor.getNome(),
                valorPagamento,
                TipoMovimentacao.SAIDA,
                CategoriaFluxo.EMPRESA
        );
    }
}