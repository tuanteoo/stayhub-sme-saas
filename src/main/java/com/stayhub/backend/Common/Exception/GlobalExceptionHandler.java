package com.stayhub.backend.Common.Exception;

import com.stayhub.backend.Common.DTO.Response.ResponseError;
import com.stayhub.backend.Common.Util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ResponseError> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatusCode().value())
                .error(errorCode.name())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(responseError);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ResponseError> handlingResourceNotFoundException(ResourceNotFoundException exception) {
        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(exception.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(value = InvalidDataException.class)
    ResponseEntity<ResponseError> handlingInvalidDataException(InvalidDataException exception) {
        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(ErrorCode.INVALID_DATA.getStatusCode().value())
                .error(ErrorCode.INVALID_DATA.name())
                .message(exception.getMessage())
                .build();

        return ResponseEntity.status(ErrorCode.INVALID_DATA.getStatusCode()).body(responseError);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ResponseError> handlingValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message("Dữ liệu đầu vào không đúng định dạng")
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(responseError);
    }
}
