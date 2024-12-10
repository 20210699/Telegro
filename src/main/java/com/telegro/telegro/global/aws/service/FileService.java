package com.telegro.telegro.global.aws.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.telegro.telegro.global.aws.dto.PresignedUrlDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;
	private final AmazonS3 amazonS3;

	public PresignedUrlDto createPresignedUrl(String prefix, Long id) {
		String path = generatePath(prefix, id);
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, path)
			.withMethod(HttpMethod.PUT)
			.withExpiration(getPresignedUrlExpiration());

//		generatePresignedUrlRequest.addRequestParameter(
//			Headers.S3_CANNED_ACL, CannedAccessControlList.PublicRead.toString());

		return PresignedUrlDto.builder()
			.url(amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString()).build();
	}

	private String generatePath(String prefix, Long id) {
		String fileName = UUID.randomUUID().toString()+"-"+String.valueOf(id);
		return String.format("%s/%s", prefix, fileName);
	}

	private Date getPresignedUrlExpiration() {
		Date expiration = new Date();
		long expTimeMillis = expiration.getTime();
		expTimeMillis += 1000 * 60 * 2;
		expiration.setTime(expTimeMillis);

		return expiration;
	}
}
