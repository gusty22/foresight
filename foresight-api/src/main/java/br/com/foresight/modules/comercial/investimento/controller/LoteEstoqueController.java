package br.com.foresight.modules.comercial.investimento.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.investimento.dto.LoteEstoqueDto;
import br.com.foresight.modules.comercial.investimento.service.LoteEstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteEstoqueController {

    private final LoteEstoqueService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoteEstoqueDto>>> listarLotes() {
        List<LoteEstoqueDto> lotes = service.listarTodosDaEmpresa();
        return ResponseEntity.ok(ApiResponse.success(lotes));
    }
}