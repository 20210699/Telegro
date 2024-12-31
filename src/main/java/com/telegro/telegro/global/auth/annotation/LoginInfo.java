package com.telegro.telegro.global.auth.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
    name = "Authorization",
    description = "JWT 인증 토큰",
    in = ParameterIn.HEADER,
    schema = @Schema(type = "string", example = "Bearer YOUR_JWT_TOKEN")
)
public @interface LoginInfo {

}
