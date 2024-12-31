package com.telegro.telegro.global.apiPayLoad.response;

import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema
public class ErrorResponse {
    @Schema(description = "에러 코드")
    private final int code;
    @Schema(description = "에러 메시지")
    private final String message;

    public static ErrorResponse of(CustomException exception) {
        return new ErrorResponse(exception.getCode(), exception.getMessage());
    }
}