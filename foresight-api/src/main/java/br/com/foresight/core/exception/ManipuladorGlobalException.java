package br.com.foresight.core.exception;

import br.com.foresight.core.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ManipuladorGlobalException {

    // Trata nossos erros de negócio customizados
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiResponse<Void>> tratarRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Trata erros de validação do Jakarta (@Valid, @NotBlank, etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> tratarErrosDeValidacao(MethodArgumentNotValidException ex) {
        String mensagensDeErro = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Erro de validação: " + mensagensDeErro));
    }

    // Fallback genérico para capturar qualquer outro erro interno não tratado (Evita stacktrace na tela)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> tratarErroGenerico(Exception ex) {
        // TODO: Enviar stacktrace para um sistema de log como DataDog/Sentry
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Ocorreu um erro interno inesperado no servidor."));
    }
}