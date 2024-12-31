package com.telegro.telegro.global.apiPayLoad.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@AllArgsConstructor
@JsonIgnoreProperties({"cause", "stackTrace", "localizedMessage", "suppressed", "httpStatus"})
public class CustomException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    public static CustomException of(Error error) {
        return new CustomException(error.getHttpStatus(), error.getCode(), error.getMessage());
    }
}