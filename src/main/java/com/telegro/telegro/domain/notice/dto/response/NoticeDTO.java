package com.telegro.telegro.domain.notice.dto.response;

import com.telegro.telegro.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeDTO(
        @Schema(description = "게시글 id")
        Long id,
        @Schema(description = "게시글 제목")
        String noticeTitle,
        @Schema(description = "게시글 파일명")
        String noticeFileName,
        @Schema(description = "게시글 작성자")
        String noticeAuthor,
        @Schema(description = "게시글 작성일")
        LocalDateTime noticeCreateDate,
        @Schema(description = "게시글 조회수")
        int viewCount
) {
    public static NoticeDTO of(Notice notice, int viewCount) {
        String noticeFileName = (notice.getNoticeFiles() != null && !notice.getNoticeFiles().isEmpty()
                && notice.getNoticeFiles().get(0) != null)
                ? notice.getNoticeFiles().get(0).getFileName()
                : null;
        return NoticeDTO.builder()
                .id(notice.getId())
                .noticeTitle(notice.getTitle())
                .noticeFileName(noticeFileName)
                .noticeAuthor(notice.getUser().getUsername())
                .noticeCreateDate(notice.getCreatedAt())
                .viewCount(viewCount)
                .build();
    }
}
