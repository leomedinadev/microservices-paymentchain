package ec.com.leodev.paymentchain.customer.exception;

import ec.com.leodev.paymentchain.customer.common.StandardApiExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<StandardApiExceptionResponse> handleUnknownHostException(UnknownHostException ex){
        StandardApiExceptionResponse response = new StandardApiExceptionResponse("Error de conexion", "error-1024", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.PARTIAL_CONTENT);
    }
}
