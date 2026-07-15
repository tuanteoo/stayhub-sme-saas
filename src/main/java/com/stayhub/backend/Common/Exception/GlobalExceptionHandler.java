package com.stayhub.backend.Common.Exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Common.DTO.Response.ResponseError;
import com.stayhub.backend.Common.Util.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ResponseError> handleInvalidDataException(InvalidDataException exception) {
        log.warn("Lỗi dữ liệu không hợp lệ: {}", exception.getMessage());

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exception.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Tham số '%s' có giá trị '%s' không đúng kiểu dữ liệu yêu cầu (%s)",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        ResponseError error = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message(message)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ResponseError> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        ResponseError error = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("Bạn không có quyền truy cập tài nguyên này")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseError> handleExpiredJwt(ExpiredJwtException ex) {
        ResponseError error = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("TOKEN_EXPIRED")
                .message("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseError> handleDataIntegrity(DataIntegrityViolationException ex) {
        ResponseError error = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("DATA_TOO_LONG")
                .message("Dữ liệu gửi lên vượt quá độ dài cho phép (ví dụ: User-Agent quá dài).")
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Lỗi parse JSON request body: {}", ex.getMessage());

        String errorMessage = "Dữ liệu yêu cầu không đúng định dạng";
        String field = "unknown";
        String rejectedValue = null;

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException ife) {
            field = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse("unknown");
            rejectedValue = ife.getValue() != null ? ife.getValue().toString() : "null";
            errorMessage = String.format("Giá trị '%s' không đúng định dạng cho trường '%s'", rejectedValue, field);
        } else if (cause instanceof JsonMappingException jme) {
            field = jme.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse("unknown");
            errorMessage = String.format("Trường '%s' có kiểu dữ liệu không phù hợp: %s", field, cause.getMessage());
        } else {
            errorMessage = "Dữ liệu yêu cầu không hợp lệ: " + cause.getMessage();
        }

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message(errorMessage)
                .build();

        return ResponseEntity.badRequest().body(responseError);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ResponseError> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.warn("Tranh chấp đặt phòng xảy ra do cập nhật đồng thời: {}", ex.getMessage());

        ResponseError responseError = ResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("CONFLICT")
                .message("Rất tiếc, phòng bạn chọn vừa được khách khác đặt nhanh tay hơn. Vui lòng thử lại hoặc chọn ngày khác!")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseError);
    }
}
