package com.telegro.telegro.domain.notice.controller;

import com.telegro.telegro.domain.notice.dto.response.CreatedNoticeDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeDetailDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeListDTO;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.domain.notice.service.NoticeService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public SuccessResponse<NoticeListDTO> getNotices(int page, int size) {
        return SuccessResponse.of(noticeService.getNotices(page,size));
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

}