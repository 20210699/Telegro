package com.telegro.telegro.domain.notice.dto.response;

import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.domain.notice.entity.NoticeFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record NoticeDetailDTO(
        @Schema(description = "게시글 id")
        Long id,
        @Schema(description = "게시글 제목")
        String noticeTitle,
        @Schema(description = "게시글 내용")
        String noticeContent,
        @Schema(description = "게시글 파일")
        List<NoticeFileDTO> noticeFiles,
        @Schema(description = "게시글 작성자")
        String noticeAuthor,
        @Schema(description = "게시글 작성일")
        LocalDateTime noticeCreateDate,
        @Schema(description = "게시글 조회수")
        int viewCount,
        @Schema(description = "게시글 팝업 설정 여부")
        Boolean isPop
) {
}
