package com.telegro.telegro.domain.notice.controller;

import com.telegro.telegro.domain.notice.dto.response.CreatedNoticeDTO;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.domain.notice.service.NoticeService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NoticeController implements NoticeControllerDocs {
    private final NoticeService noticeService;

    @PostMapping("/api/notices")
    public SuccessResponse<CreatedNoticeDTO> createNotice(Long id, Notice request) {
        return SuccessResponse.of(noticeService.createNotice(id, request));
    }

}
