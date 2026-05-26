package br.com.foresight.modules.comercial.produto.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.produto.dto.ProdutoDto;
import br.com.foresight.modules.comercial.produto.dto.ProdutoRequest;
import br.com.foresight.modules.comercial.produto.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ProdutoDto>> criar(@RequestBody @Valid ProdutoRequest request) {
        ProdutoDto produtoCriado = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(produtoCriado, "Produto cadastrado com sucesso."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProdutoDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(service.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoDto>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    // =========================================================================
    // NOVO: ENDPOINT PARA ESCANEAMENTO DE CÓDIGO DE BARRAS
    // =========================================================================
    @GetMapping("/barcode/{codigo}")
    public ResponseEntity<ApiResponse<ProdutoDto>> buscarPorCodigoBarras(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorCodigoBarras(codigo)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoDto>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ProdutoRequest request) {
        ProdutoDto produtoAtualizado = service.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(produtoAtualizado, "Produto atualizado com sucesso."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Produto excluído com sucesso do catálogo."));
    }
}