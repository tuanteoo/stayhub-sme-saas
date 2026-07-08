package com.stayhub.backend.Common.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseError{
    private LocalDateTime timestamp;
    private int status;
    private int code;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
}
