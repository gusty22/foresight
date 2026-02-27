package br.com.foresight.modules.backoffice.empresa.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.backoffice.empresa.dto.AlterarStatusEmpresaRequest;
import br.com.foresight.modules.backoffice.empresa.dto.EmpresaGlobalDto;
import br.com.foresight.modules.backoffice.empresa.service.BackofficeEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/empresas")
@RequiredArgsConstructor
public class BackofficeEmpresaController {

    private final BackofficeEmpresaService backofficeService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmpresaGlobalDto>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.success(backofficeService.listarTodasEmpresas()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaGlobalDto>> alterarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AlterarStatusEmpresaRequest request) {

        EmpresaGlobalDto atualizada = backofficeService.alterarStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(atualizada, "Status da empresa atualizado com sucesso."));
    }
}