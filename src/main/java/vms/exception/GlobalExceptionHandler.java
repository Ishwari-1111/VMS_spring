package vms.exception;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
}
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
}
@ExceptionHandler(NoSuchElementException.class)
public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
}
}
    

