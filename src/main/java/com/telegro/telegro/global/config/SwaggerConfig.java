package com.telegro.telegro.global.config;

import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.response.ErrorResponse;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(Type.HTTP)
                .in(In.HEADER)
                .name("Authorization")
                .scheme("Bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearer-key");

        return new OpenAPI()
                .info(new Info()
                        .title("Telegro API")
                        .version("1.0.0")
                        .description("Telegro API 문서입니다."))
                .components(new Components()
                        .addSecuritySchemes("bearer-key", apiKey)
                ).addSecurityItem(securityRequirement);
    }

    @Bean
    public OpenApiCustomizer openApiCustomiser() {

        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class));

        return openApi -> openApi.getPaths().values()
                .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    ApiResponse unauthorizedResponse = new ApiResponse()
                            .description("유저 인증 실패")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType()
                                            .schema(resolvedSchema.schema)
                                            .example(ErrorResponse.of(CustomException.of(Error.INVALID_TOKEN_ERROR)))));
                    ApiResponse notFoundResponse = new ApiResponse()
                            .description("리소스를 찾을 수 없음")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType()
                                            .schema(resolvedSchema.schema)
                                            .example(ErrorResponse.of(CustomException.of(Error.NOT_FOUND_ERROR)))));

                    operation.getResponses().addApiResponse("401", unauthorizedResponse);
                    operation.getResponses().addApiResponse("404", notFoundResponse);
                }));
    };
}