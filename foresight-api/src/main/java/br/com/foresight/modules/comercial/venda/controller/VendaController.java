package br.com.foresight.modules.comercial.venda.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.venda.dto.VendaDto;
import br.com.foresight.modules.comercial.venda.dto.VendaRequest;
import br.com.foresight.modules.comercial.venda.service.VendaService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService service;

    // 1. CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<VendaDto>> realizarVenda(@RequestBody @Valid VendaRequest request) {
        VendaDto vendaCriada = service.realizarVenda(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vendaCriada, "Venda finalizada com sucesso."));
    }

    // 2. READ (List)
    @GetMapping
    public ResponseEntity<ApiResponse<List<VendaDto>>> historico() {
        return ResponseEntity.ok(ApiResponse.success(service.listarHistorico()));
    }

    // 3. READ (Detail)
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ApiResponse<VendaDto>> obterDetalhes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarDetalhesVenda(id)));
    }

    // 4. UPDATE (Ação Específica: Confirmar Pagamento - O EXATO ENDPOINT QUE FALTAVA)
    @PutMapping("/{id}/confirmar-pagamento")
    public ResponseEntity<ApiResponse<VendaDto>> confirmarPagamento(@PathVariable Long id) {
        VendaDto vendaAtualizada = service.confirmarPagamento(id);
        return ResponseEntity.ok(ApiResponse.success(vendaAtualizada, "Pagamento confirmado. Fluxo de caixa atualizado."));
    }

    // 5. DELETE (Estorno Financeiro e Devolução de Estoque)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelarVenda(@PathVariable Long id) {
        service.excluirOuEstornarVenda(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Venda cancelada com sucesso. Estoque e financeiro revertidos."));
    }

    // EXTRA: Geração de Relatório
    @GetMapping("/{id}/comprovante")
    public void gerarComprovante(@PathVariable Long id, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        String headerKey = "Content-Disposition";
        String headerValue = "inline; filename=comprovante_venda_" + id + "_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        service.gerarPdfVenda(id, response);
    }
}