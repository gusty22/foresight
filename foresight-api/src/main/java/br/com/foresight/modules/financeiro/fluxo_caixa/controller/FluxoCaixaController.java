package br.com.foresight.modules.financeiro.fluxo_caixa.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.financeiro.fluxo_caixa.dto.FluxoCaixaDto;
import br.com.foresight.modules.financeiro.fluxo_caixa.dto.FluxoCaixaRequest;
import br.com.foresight.modules.financeiro.fluxo_caixa.service.FluxoCaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fluxo-caixa")
@RequiredArgsConstructor
public class FluxoCaixaController {

    private final FluxoCaixaService service;

    @PostMapping
    public ResponseEntity<ApiResponse<FluxoCaixaDto>> registrar(@RequestBody @Valid FluxoCaixaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.registrarMovimentacao(request), "Movimentação registrada com sucesso."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FluxoCaixaDto>>> obterHistorico() {
        return ResponseEntity.ok(ApiResponse.success(service.listarHistorico()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FluxoCaixaDto>> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    // Padrão REST/Fintech: Exclusão física não existe, aplicamos uma ação customizada (estorno)
    @PostMapping("/{id}/estornar")
    public ResponseEntity<ApiResponse<FluxoCaixaDto>> estornar(@PathVariable Long id) {
        FluxoCaixaDto estorno = service.estornarMovimentacao(id);
        return ResponseEntity.ok(ApiResponse.success(estorno, "Lançamento estornado com sucesso."));
    }

    @GetMapping("/resumo")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> resumo() {
        return ResponseEntity.ok(ApiResponse.success(service.obterResumoFinanceiro()));
    }
}