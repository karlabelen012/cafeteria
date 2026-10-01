package cl.duoc.cafeteria.notificaciones.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centraliza el formato de error de toda la API de este microservicio:
 * {timestamp, status, error, message, fieldErrors} (ver docs/EP2_PLAN.md
 * seccion 3.8 / Fase 1). fieldErrors solo viene presente en errores de
 * validacion (400).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::aFieldError)
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", fieldErrors);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Cubre tanto AccessDeniedException "clasica" como AuthorizationDeniedException
    // (Spring Security 6, la que lanzan los interceptores de @PreAuthorize), ya que
    // esta ultima extiende de la primera. Sin este handler, un 403 por @PreAuthorize
    // vuelve con el cuerpo vacio por defecto de Spring Security en vez del formato
    // {timestamp,status,error,message} usado en el resto de la API.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoDenegado(AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta accion", null);
    }

    private Map<String, String> aFieldError(FieldError error) {
        Map<String, String> detalle = new LinkedHashMap<>();
        detalle.put("campo", error.getField());
        detalle.put("mensaje", error.getDefaultMessage());
        return detalle;
    }

    private ResponseEntity<Map<String, Object>> construir(HttpStatus status, String message,
            List<Map<String, String>> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (fieldErrors != null) {
            body.put("fieldErrors", fieldErrors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
