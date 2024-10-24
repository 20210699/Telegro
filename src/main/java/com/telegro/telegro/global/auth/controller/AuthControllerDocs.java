package com.telegro.telegro.global.auth.controller;

import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.dto.request.LoginRequestDto;
import com.telegro.telegro.global.auth.dto.request.SignUpRequestDto;
import com.telegro.telegro.global.auth.dto.response.LoginDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;


public interface AuthControllerDocs {
    @Operation(summary = "자체 회원 가입")
    public SuccessResponse<?> signup(@RequestBody SignUpRequestDto signUpRequestDto);

    @Operation(summary = "자체 로그인")
    public SuccessResponse<LoginDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response);

}
