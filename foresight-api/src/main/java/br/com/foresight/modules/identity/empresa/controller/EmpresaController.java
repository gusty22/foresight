package br.com.foresight.modules.identity.empresa.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.identity.empresa.dto.EmpresaDto;
import br.com.foresight.modules.identity.empresa.dto.EmpresaRequest;
import br.com.foresight.modules.identity.empresa.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService service;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaDto>> criar(@RequestBody @Valid EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.criar(request), "Empresa cadastrada com sucesso."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpresaDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(service.listarMinhasEmpresas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaDto>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaDto>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid EmpresaRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.atualizar(id, request), "Dados da empresa atualizados."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Empresa removida com sucesso."));
    }
}