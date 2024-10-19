package com.telegro.telegro.domain.notice.dto.response;

import com.telegro.telegro.domain.notice.entity.NoticeFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NoticeFileDTO (
        @Schema(description = "게시글 파일 id")
        Long id,
        @Schema(description = "게시글 파일명")
        String fileName,
        @Schema(description = "게시글 파일 URL")
        String fileUrl
){
    public static NoticeFileDTO of(NoticeFile noticeFile) {
        return NoticeFileDTO.builder()
                .id(noticeFile.getId())
                .fileName(noticeFile.getFileName())
                .fileUrl(noticeFile.getFileUrl())
                .build();
    }
}
