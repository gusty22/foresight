package br.com.foresight.modules.backoffice.empresa.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.backoffice.empresa.dto.AlterarStatusEmpresaRequest;
import br.com.foresight.modules.backoffice.empresa.dto.EmpresaGlobalDto;
import br.com.foresight.modules.backoffice.empresa.service.BackofficeEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/backoffice/empresas")
@RequiredArgsConstructor
public class BackofficeEmpresaController {

    private final BackofficeEmpresaService backofficeService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<EmpresaGlobalDto>>> listarPaginado(
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<EmpresaGlobalDto> resultado = backofficeService.listarEmpresasPaginado(termo, pageable);
        return ResponseEntity.ok(ApiResponse.success(resultado));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaGlobalDto>> alterarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AlterarStatusEmpresaRequest request) {

        EmpresaGlobalDto atualizada = backofficeService.alterarStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(atualizada, "Governança: Status da empresa atualizado com sucesso."));
    }

    @PostMapping("/{id}/impersonate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> gerarTokenSuporte(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(backofficeService.gerarTokenImpersonation(id)));
    }
}