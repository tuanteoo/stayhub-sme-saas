package com.stayhub.backend.Common.DTO.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> implements Serializable {
    private final int status;
    private final String message;
    private T data;

    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
