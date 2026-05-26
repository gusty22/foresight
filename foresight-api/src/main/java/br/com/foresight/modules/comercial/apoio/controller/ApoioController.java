package br.com.foresight.modules.comercial.apoio.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.apoio.dto.CategoriaProdutoDto;
import br.com.foresight.modules.comercial.apoio.dto.FornecedorDto;
import br.com.foresight.modules.comercial.apoio.service.ApoioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/apoio")
@RequiredArgsConstructor
public class ApoioController {

    private final ApoioService apoioService;

    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<CategoriaProdutoDto>>> listarCategorias() {
        return ResponseEntity.ok(ApiResponse.success(apoioService.listarCategorias()));
    }

    @GetMapping("/fornecedores")
    public ResponseEntity<ApiResponse<List<FornecedorDto>>> listarFornecedores() {
        return ResponseEntity.ok(ApiResponse.success(apoioService.listarFornecedores()));
    }
}