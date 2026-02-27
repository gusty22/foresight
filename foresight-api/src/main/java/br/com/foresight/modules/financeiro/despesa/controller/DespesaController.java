package br.com.foresight.modules.financeiro.despesa.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.financeiro.despesa.dto.DespesaDto;
import br.com.foresight.modules.financeiro.despesa.dto.DespesaRequest;
import br.com.foresight.modules.financeiro.despesa.service.DespesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/despesas")
@RequiredArgsConstructor
public class DespesaController {

    private final DespesaService service;

    @PostMapping
    public ResponseEntity<ApiResponse<DespesaDto>> criar(@RequestBody @Valid DespesaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.criar(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DespesaDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(service.listarPorEmpresa()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Despesa excluída (e estornada do caixa) com sucesso."));
    }
}