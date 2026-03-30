package com.telegro.telegro.domain.notice.controller;

import com.telegro.telegro.domain.notice.dto.response.CreatedNoticeDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeDetailDTO;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.domain.notice.service.NoticeService;
import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NoticeController implements NoticeControllerDocs {
    private final NoticeService noticeService;

    @PostMapping("/api/notices")
    public SuccessResponse<CreatedNoticeDTO> createNotice(Long id, Notice request) {
        return SuccessResponse.of(noticeService.createNotice(id, request));
    }

    @GetMapping("/notices")
    public SuccessResponse<CursorPagedResponse<NoticeDTO>> getNotices(LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return SuccessResponse.of(noticeService.getNotices(cursorCreatedAt, cursorId, size));
    }

    @GetMapping("/notices/{noticeId}")
    public SuccessResponse<NoticeDetailDTO> getNoticeDetail(Long noticeId) {
        return SuccessResponse.of(noticeService.getNoticeDetail(noticeId));
    }

    @DeleteMapping("/api/notices/{noticeId}")
    public SuccessResponse<Boolean> deleteNotice(Long id, Long noticeId) {
        noticeService.deleteNotice(id, noticeId);
        return SuccessResponse.of();
    }

    @PatchMapping("/api/notices/{noticeId}")
    public SuccessResponse<NoticeDetailDTO> updateNotice(Long id, Long noticeId, Notice request) {
        return SuccessResponse.of(noticeService.updateNotice(id, noticeId, request));
    }

    @PostMapping("/api/notices/{noticeId}/popup")
    public SuccessResponse<Boolean> setPopNotice(Long id, Long noticeId) {
        noticeService.setPopNotice(id, noticeId);
        return SuccessResponse.of();
    }

    @GetMapping("/notices/popup")
    public SuccessResponse<NoticeDetailDTO> getPopNotice() {
        return SuccessResponse.of(noticeService.getPopNotice());
    }

}
