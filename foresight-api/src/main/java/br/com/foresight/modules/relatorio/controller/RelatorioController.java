package br.com.foresight.modules.relatorio.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.relatorio.dto.*;
import br.com.foresight.modules.relatorio.service.RelatorioFinanceiroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioFinanceiroService relatorioService;

    @GetMapping("/saude-financeira")
    public ResponseEntity<ApiResponse<RelatorioSaudeDto>> obterSaude() {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.calcularSaudeFinanceira()));
    }

    @GetMapping("/lucratividade")
    public ResponseEntity<ApiResponse<List<LucratividadeDto>>> obterLucratividade() {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.gerarRelatorioLucratividade()));
    }

    @GetMapping("/ponto-equilibrio")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterPontoEquilibrio() {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.calcularPontoEquilibrio()));
    }
    @GetMapping("/dre")
    public ResponseEntity<ApiResponse<DreDto>> obterDre(
            @RequestParam(required = false, defaultValue = "MENSAL") String periodicidade,
            @RequestParam(required = false) Integer periodoValor,
            @RequestParam(required = false) Integer ano) {

        return ResponseEntity.ok(ApiResponse.success(relatorioService.gerarDre(periodicidade, periodoValor, ano)));
    }

    @GetMapping("/ranking-vendas")
    public ResponseEntity<ApiResponse<List<RankingVendasDto>>> obterRankingVendas() {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.gerarRankingVendas()));
    }

    @GetMapping("/inadimplencia")
    public ResponseEntity<ApiResponse<List<InadimplenciaDto>>> obterInadimplencia() {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.gerarRelatorioInadimplencia()));
    }
}