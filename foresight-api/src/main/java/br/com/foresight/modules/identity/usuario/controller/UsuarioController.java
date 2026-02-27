package br.com.foresight.modules.identity.usuario.controller;

import br.com.foresight.core.web.ApiResponse;
import br.com.foresight.modules.identity.usuario.dto.AtualizarPerfilRequest;
import br.com.foresight.modules.identity.usuario.dto.AtualizarSenhaRequest;
import br.com.foresight.modules.identity.usuario.dto.PerfilUsuarioDto;
import br.com.foresight.modules.identity.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PerfilUsuarioDto>> obterPerfil() {
        return ResponseEntity.ok(ApiResponse.success(service.obterMeuPerfil()));
    }

    @PutMapping("/me/perfil")
    public ResponseEntity<ApiResponse<PerfilUsuarioDto>> atualizarPerfil(@RequestBody @Valid AtualizarPerfilRequest request) {
        PerfilUsuarioDto perfilAtualizado = service.atualizarMeuPerfil(request);
        return ResponseEntity.ok(ApiResponse.success(perfilAtualizado, "Perfil atualizado com sucesso."));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<ApiResponse<Void>> atualizarSenha(@RequestBody @Valid AtualizarSenhaRequest request) {
        service.atualizarMinhaSenha(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Senha alterada com sucesso."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> excluirMinhaConta() {
        service.deletarMinhaConta();
        return ResponseEntity.ok(ApiResponse.success(null, "Sua conta e todos os dados vinculados foram excluídos permanentemente."));
    }
}