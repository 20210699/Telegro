package com.telegro.telegro.global.aws.dto;

import lombok.Builder;

@Builder
public record PresignedUrlDto(
	String url
) {
}
