package br.com.foresight.modules.comercial.cliente.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.comercial.cliente.dto.ClienteDto;
import br.com.foresight.modules.comercial.cliente.dto.ClienteRequest;
import br.com.foresight.modules.comercial.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteDto>> criar(@RequestBody @Valid ClienteRequest request) {
        ClienteDto cliente = service.salvar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cliente, "Cliente cadastrado com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteDto>> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ClienteRequest request) {
        ClienteDto cliente = service.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(cliente, "Cliente atualizado com sucesso."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteDto>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(service.listarPorEmpresa()));
    }

    @GetMapping("/{id}/detalhes")
    public ResponseEntity<ApiResponse<ClienteDto>> obterPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarPorId(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cliente excluído com sucesso."));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ClienteDto>>> buscarParaVenda(@RequestParam String termo) {
        return ResponseEntity.ok(ApiResponse.success(service.buscarClientesAutocomplete(termo)));
    }
}