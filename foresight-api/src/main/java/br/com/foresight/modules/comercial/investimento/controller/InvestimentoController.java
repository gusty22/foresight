package br.com.foresight.modules.comercial.investimento.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.investimento.dto.InvestidorDto;
import br.com.foresight.modules.comercial.investimento.dto.PagamentoInvestidorRequest;
import br.com.foresight.modules.comercial.investimento.dto.RelatorioInvestidorDto;
import br.com.foresight.modules.comercial.investimento.dto.RepasseInvestidorDto;
import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import br.com.foresight.modules.comercial.investimento.service.InvestimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investimentos")
@RequiredArgsConstructor
public class InvestimentoController {

    private final InvestimentoService investimentoService;

    // --- Endpoints de Investidores (CRUD) ---

    @GetMapping("/investidores")
    public ResponseEntity<ApiResponse<List<InvestidorDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(investimentoService.listarInvestidores()));
    }
    @GetMapping("/investidores/{id}/relatorio")
    public ResponseEntity<ApiResponse<RelatorioInvestidorDto>> relatorioCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(investimentoService.gerarRelatorioInvestidor(id)));
    }

    @GetMapping("/investidores/{id}")
    public ResponseEntity<ApiResponse<InvestidorDto>> buscarPorId(@PathVariable Long id) {
        return investimentoService.buscarPorId(id)
                .map(i -> ResponseEntity.ok(ApiResponse.success(
                        new InvestidorDto(i.getId(), i.getNome(), i.getTelefone(), i.getChavePix(), i.getStatus()),
                        "Investidor encontrado"
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/investidores")
    public ResponseEntity<ApiResponse<InvestidorDto>> salvar(@RequestBody @Valid InvestidorDto dto) { // Adicionado @Valid
        Investidor investidor = Investidor.builder()
                .nome(dto.nome())
                .telefone(dto.telefone())
                .chavePix(dto.chavePix())
                .status("ATIVO")
                .build();

        Investidor salvo = investimentoService.salvarInvestidor(investidor);

        InvestidorDto resposta = new InvestidorDto(
                salvo.getId(), salvo.getNome(), salvo.getTelefone(), salvo.getChavePix(), salvo.getStatus()
        );

        return ResponseEntity.ok(ApiResponse.success(resposta, "Investidor salvo com sucesso"));
    }

    @PutMapping("/investidores/{id}")
    public ResponseEntity<ApiResponse<InvestidorDto>> atualizar(@PathVariable Long id, @RequestBody @Valid InvestidorDto dto) { // Adicionado @Valid
        Investidor atualizado = investimentoService.atualizarInvestidor(id, dto);

        InvestidorDto resposta = new InvestidorDto(
                atualizado.getId(), atualizado.getNome(), atualizado.getTelefone(), atualizado.getChavePix(), atualizado.getStatus()
        );

        return ResponseEntity.ok(ApiResponse.success(resposta, "Investidor atualizado com sucesso"));
    }
    // Adicione nos imports: import br.com.foresight.modules.comercial.investimento.dto.PagamentoInvestidorRequest;

    @PostMapping("/investidores/{id}/pagar")
    public ResponseEntity<ApiResponse<String>> pagarInvestidor(@PathVariable Long id, @RequestBody @Valid PagamentoInvestidorRequest request) {
        investimentoService.pagarInvestidor(id, request.valor());
        return ResponseEntity.ok(ApiResponse.success("OK", "Pagamento registrado e abatido do fluxo de caixa com sucesso."));
    }

    @DeleteMapping("/investidores/{id}")
    public ResponseEntity<ApiResponse<String>> inativar(@PathVariable Long id) {
        investimentoService.inativarInvestidor(id);

        // CORREÇÃO: Passamos uma string no primeiro argumento para evitar o erro de tipo Void/T
        return ResponseEntity.ok(ApiResponse.success("OK", "Investidor inativado com sucesso"));
    }

    // --- Endpoints de Repasses ---

    @GetMapping("/repasses/pendentes")
    public ResponseEntity<ApiResponse<List<RepasseInvestidorDto>>> listarPendentes() {
        return ResponseEntity.ok(ApiResponse.success(investimentoService.listarRepassesPendentes(), "Repasses listados com sucesso"));
    }
}