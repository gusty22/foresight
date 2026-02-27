package br.com.foresight.modules.dashboard.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.dashboard.service.DashboardService;
import br.com.foresight.modules.dashboard.dto.DashboardResumoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/resumo")
    public ResponseEntity<ApiResponse<DashboardResumoDto>> obterResumo() {
        return ResponseEntity.ok(ApiResponse.success(service.obterResumo()));
    }
}