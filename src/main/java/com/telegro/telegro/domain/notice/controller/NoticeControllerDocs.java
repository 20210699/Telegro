package com.telegro.telegro.domain.notice.controller;

import com.telegro.telegro.domain.notice.dto.response.CreatedNoticeDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeDetailDTO;
import com.telegro.telegro.domain.notice.dto.response.NoticeListDTO;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface NoticeControllerDocs {
    @Operation(description = "공지사항 게시글을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 작성 성공")
    public SuccessResponse<CreatedNoticeDTO> createNotice(@LoginInfo Long id, @RequestBody Notice request);

    @Operation(description = "공지사항 게시글의 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    public SuccessResponse<NoticeListDTO> getNotices(@RequestParam(value = "page") int page, @RequestParam(value = "size") int size);

    @Operation(description = "공지사항 게시글을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공")
    public SuccessResponse<NoticeDetailDTO> getNoticeDetail(@PathVariable Long noticeId);

    @Operation(description = "공지사항 게시글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 삭제 성공")
    public SuccessResponse<Boolean> deleteNotice(@LoginInfo Long id, @PathVariable Long noticeId);

    @Operation(description = "공지사항 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    public SuccessResponse<NoticeDetailDTO> updateNotice(@LoginInfo Long id, @PathVariable Long noticeId, @RequestBody Notice request);

    @Operation(description = "공지사항 팝업 게시글을 설정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 팝업 설정 성공")
    public SuccessResponse<Boolean> setPopNotice(@LoginInfo Long id, @PathVariable Long noticeId);

    @Operation(description = "공지사항 팝업 게시글을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "팝업 게시글 상세 조회 성공")
    public SuccessResponse<NoticeDetailDTO> getPopNotice();
}
