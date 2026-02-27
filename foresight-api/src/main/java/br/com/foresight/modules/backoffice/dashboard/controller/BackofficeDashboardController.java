package br.com.foresight.modules.backoffice.dashboard.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.backoffice.dashboard.dto.DashboardGlobalDto;
import br.com.foresight.modules.backoffice.dashboard.service
        .BackofficeDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backoffice/dashboard")
@RequiredArgsConstructor
public class BackofficeDashboardController {

    private final BackofficeDashboardService dashboardService;

    @GetMapping("/resumo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardGlobalDto>> obterResumoGlobal() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.calcularResumoGlobal()));
    }
}
