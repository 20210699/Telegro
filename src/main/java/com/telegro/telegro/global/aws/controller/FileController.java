package com.telegro.telegro.global.aws.controller;

import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import com.telegro.telegro.global.aws.dto.PresignedUrlDto;
import com.telegro.telegro.global.aws.service.FileService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

	@PostMapping
	public SuccessResponse<PresignedUrlDto> getPresignedUrl(
			@LoginInfo Long id,
			@RequestParam
			@Parameter(description = "이미지 저장 시 prefix",
					examples = {
							@ExampleObject(name = "product 관련 이미지", value = "product"),
							@ExampleObject(name = "notice 관련 파일", value = "notice")
					}) String prefix,
			@RequestParam(required = false)
			@Parameter(description = "파일명") String fileName) {

		return SuccessResponse.of(fileService.createPresignedUrl(prefix, id, fileName));
	}
}
