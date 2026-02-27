package br.com.foresight.modules.identity.auth;

import br.com.foresight.core.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AutenticacaoService service;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest dados) {
        return ResponseEntity.ok(ApiResponse.success(service.login(dados), "Login realizado com sucesso."));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<TokenResponse>> registrar(@RequestBody @Valid RegistroRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.registrar(dados), "Conta e Empresa criadas com sucesso."));
    }
}