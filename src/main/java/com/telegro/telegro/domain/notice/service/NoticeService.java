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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;

    @Transactional
    public CreatedNoticeDTO createNotice(Long id, Notice request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

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

    @Transactional
    public NoticeListDTO getNotices(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notice> notices = noticeRepository.findAll(pageRequest);
        boolean isLast = notices.isLast();
        int totalPage = notices.getTotalPages();
        long totalElement = notices.getTotalElements();

        List<NoticeDTO> noticeDTOS = notices.getContent().stream()
                .map(notice -> NoticeDTO.of(notice, notice.getViewCount())).toList();

        return NoticeListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .notices(noticeDTOS)
                .build();
    }

    @Transactional
    public NoticeDetailDTO getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

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
                .build();
    }

    @Transactional
    public void deleteNotice(Long id, Long noticeId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        noticeRepository.deleteById(noticeId);
    }
}
