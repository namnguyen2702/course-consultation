package vn.coursebooking.course;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception
    ){
        Map<String, String> errors = new LinkedHashMap<>();

        for (var error : exception.getBindingResult().getFieldErrors()){
            errors.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
