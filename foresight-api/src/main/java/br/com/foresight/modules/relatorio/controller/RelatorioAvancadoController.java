package br.com.foresight.modules.relatorio.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.relatorio.dto.TransacaoRelatorioDto;
import br.com.foresight.modules.relatorio.service.RelatorioAvancadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios/avancado")
@RequiredArgsConstructor
public class RelatorioAvancadoController {

    private final RelatorioAvancadoService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransacaoRelatorioDto>>> buscar(
            @RequestParam(value = "contexto", required = false, defaultValue = "FLUXO") String contexto,
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String categoria,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                service.buscarDados(contexto, termo, dataInicio, dataFim, tipo, categoria, pageable)
        ));
    }

    @GetMapping(value = "/export/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false, defaultValue = "FLUXO") String contexto,
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String categoria) {

        byte[] pdf = service.exportarParaPdf(contexto, termo, dataInicio, dataFim, tipo, categoria);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=relatorio_" + contexto.toLowerCase() + ".pdf")
                .body(pdf);
    }
}