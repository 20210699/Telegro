package com.telegro.telegro.domain.notice.service;

import com.telegro.telegro.domain.notice.dto.response.*;
import com.telegro.telegro.domain.notice.entity.Notice;
import com.telegro.telegro.domain.notice.entity.NoticeFile;
import com.telegro.telegro.domain.notice.repository.NoticeFileRepository;
import com.telegro.telegro.domain.notice.repository.NoticeRepository;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;
import com.telegro.telegro.global.common.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final RedisUtil redisUtil;

    @Transactional
    public CreatedNoticeDTO createNotice(Long id, Notice request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .context(request.getContext())
                .user(user)
                .build();

        if (request.getNoticeFiles() != null) {
            for (NoticeFile noticeFile : request.getNoticeFiles()) {
                noticeFile.setNotice(notice);
            }
            notice.setNoticeFiles(request.getNoticeFiles());
        }

        Notice savedNotice = noticeRepository.save(notice);

        return CreatedNoticeDTO.builder().id(savedNotice.getId()).build();
    }

    @Transactional(readOnly = true)
    public CursorPagedResponse<NoticeDTO> getNotices(LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }
        if (size < 1) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        PageRequest pageRequest = PageRequest.of(0, size + 1, Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));
        List<Notice> notices = noticeRepository.findNoticesWithCursor(cursorCreatedAt, cursorId, pageRequest);

        boolean isLast = notices.size() <= size;
        List<Notice> pagedNotices = isLast ? notices : new ArrayList<>(notices.subList(0, size));
        Notice nextCursorNotice = isLast ? null : pagedNotices.get(pagedNotices.size() - 1);

        List<NoticeDTO> noticeDTOS = pagedNotices.stream()
                .map(notice -> NoticeDTO.of(notice, notice.getViewCount())).toList();

        return CursorPagedResponse.of(
                !isLast,
                CursorPagedResponse.cursorOf(
                        nextCursorNotice != null ? nextCursorNotice.getId() : null,
                        nextCursorNotice != null ? nextCursorNotice.getCreatedAt() : null
                ),
                noticeDTOS
        );
    }

    @Transactional
    public NoticeDetailDTO getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> CustomException.of(Error.NOTICE_NOT_FOUND));

        notice.setViewCount(notice.getViewCount() + 1);

        noticeRepository.save(notice);

        List<NoticeFileDTO> noticeFiles = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileDTO::of).toList();

        return NoticeDetailDTO.builder()
                .id(notice.getId())
                .noticeTitle(notice.getTitle())
                .noticeContent(notice.getContext())
                .noticeFiles(noticeFiles)
                .noticeAuthor(notice.getUser().getUsername())
                .noticeCreateDate(notice.getCreatedAt())
                .viewCount(notice.getViewCount())
                .isPop(Long.valueOf(redisUtil.getData("popup_notice_id")).equals(notice.getId()))
                .build();
    }

    @Transactional
    public void deleteNotice(Long id, Long noticeId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        noticeRepository.deleteById(noticeId);
    }

    @Transactional
    public NoticeDetailDTO updateNotice(Long userId, Long noticeId, Notice request) {

        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        // ADMIN 권한 확인
        if (!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        // 수정할 공지사항 가져오기
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> CustomException.of(Error.NOTICE_NOT_FOUND));

        // 제목 수정 (존재할 경우)
        if (request.getTitle() != null) {
            notice.setTitle(request.getTitle());
        }

        // 내용 수정 (존재할 경우)
        if (request.getContext() != null) {
            notice.setContext(request.getContext());
        }

        // 파일 목록 수정 (존재할 경우)
        if (request.getNoticeFiles() != null && !request.getNoticeFiles().isEmpty()) {
            // 1. 기존 NoticeFile에서 제거 (개별 삭제)
            notice.getNoticeFiles().forEach(noticeFile -> noticeFile.setNotice(null)); // 자식 엔티티 관계 해제
            notice.getNoticeFiles().clear(); // 기존 파일들 비우기

            // 2. 새로운 NoticeFile 추가
            for (NoticeFile noticeFile : request.getNoticeFiles()) {
                noticeFile.setNotice(notice);  // 부모 엔티티 설정
                notice.getNoticeFiles().add(noticeFile);  // 새 파일 추가
            }
        }

        // 변경 사항 저장
        Notice updatedNotice = noticeRepository.save(notice);

        // 파일 DTO 변환
        List<NoticeFileDTO> noticeFiles = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileDTO::of)
                .toList();

        // 수정된 NoticeDTO 반환
        return NoticeDetailDTO.builder()
                .id(updatedNotice.getId())
                .noticeTitle(updatedNotice.getTitle())
                .noticeContent(updatedNotice.getContext())
                .noticeFiles(noticeFiles)
                .noticeAuthor(updatedNotice.getUser().getUsername())
                .noticeCreateDate(updatedNotice.getCreatedAt())
                .viewCount(updatedNotice.getViewCount())
                .isPop(Long.valueOf(redisUtil.getData("popup_notice_id")).equals(updatedNotice.getId()))
                .build();
    }


    public void setPopNotice(Long id, Long noticeId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        noticeRepository.findById(noticeId).orElseThrow(() -> CustomException.of(Error.NOTICE_NOT_FOUND));

        String key = "popup_notice_id";

        if (redisUtil.existData(key)) {
            redisUtil.deleteData(key);
        }

        redisUtil.setData(key, noticeId.toString());
        log.info("팝업 공지가 공지 ID {}로 설정되었습니다.", noticeId);
    }

    public NoticeDetailDTO getPopNotice() {
        Long noticeId = Long.valueOf(redisUtil.getData("popup_notice_id"));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> CustomException.of(Error.NOTICE_NOT_FOUND));

        List<NoticeFileDTO> noticeFiles = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileDTO::of)
                .toList();

        return NoticeDetailDTO.builder()
                .id(notice.getId())
                .noticeTitle(notice.getTitle())
                .noticeContent(notice.getContext())
                .noticeFiles(noticeFiles)
                .noticeAuthor(notice.getUser().getUsername())
                .noticeCreateDate(notice.getCreatedAt())
                .viewCount(notice.getViewCount())
                .isPop(true)
                .build();
    }
}
