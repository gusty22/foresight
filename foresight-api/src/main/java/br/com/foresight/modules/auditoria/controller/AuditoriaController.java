package br.com.foresight.modules.auditoria.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.auditoria.dto.LogAuditoriaDto;
import br.com.foresight.modules.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LogAuditoriaDto>>> listarLogs() {
        return ResponseEntity.ok(ApiResponse.success(service.listarLogs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LogAuditoriaDto>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }
}