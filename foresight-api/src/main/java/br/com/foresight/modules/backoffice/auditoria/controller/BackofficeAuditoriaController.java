package br.com.foresight.modules.backoffice.auditoria.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.backoffice.auditoria.dto.LogAuditoriaDto;
import br.com.foresight.modules.backoffice.auditoria.service.BackofficeAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/backoffice/auditoria")
@RequiredArgsConstructor
public class BackofficeAuditoriaController {

    private final BackofficeAuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')") // BLINDAGEM DE SEGURANÇA OBRIGATÓRIA
    public ResponseEntity<ApiResponse<Page<LogAuditoriaDto>>> listarLogs(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @PageableDefault(size = 50, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LogAuditoriaDto> logs = auditoriaService.listarLogsGlobais(termo, acao, dataInicio, dataFim, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}