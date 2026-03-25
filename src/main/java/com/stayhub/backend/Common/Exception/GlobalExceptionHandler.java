package com.stayhub.backend.Common.Exception;

import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Common.DTO.Response.ResponseError;
import com.stayhub.backend.Common.Util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ResponseError> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatusCode().value())
                .code(errorCode.getCode())
                .error(errorCode.getStatusCode().getReasonPhrase())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(responseError);
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


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ResponseError> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
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

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ResponseError> handlingGlobalException(Exception exception) {
        log.error("Unhandled Exception: ", exception);

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode().value())
                .error(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode().getReasonPhrase())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(responseError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseError> handleAuthenticationException(AuthenticationException e) {
        log.error("Lỗi đăng nhập bắt được: {}", e.getClass().getName());

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message("Email hoặc mật khẩu không chính xác!")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);
    }
}
