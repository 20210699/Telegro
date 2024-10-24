package com.telegro.telegro.domain.user.service;

import com.telegro.telegro.domain.user.entity.Hit;
import com.telegro.telegro.domain.user.repository.HitRepository;
import com.telegro.telegro.global.common.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {
    private final HitRepository hitRepository;

    public void recordAnonymousVisit(HttpServletRequest request, HttpServletResponse response) {
        LocalDate today = LocalDate.now();

        // 쿠키에서 익명 사용자 ID를 가져옵니다.
        Optional<Cookie> optionalCookie = CookieUtil.getCookie(request, "anonymousUserId");
        String anonymousUserId;

        if (optionalCookie.isPresent()) {
            log.info("쿠키 이름 : {}", optionalCookie.get().getName());
            log.info("쿠키 값 : {}", optionalCookie.get().getValue());

            anonymousUserId = optionalCookie.get().getValue();
        } else {
            // 익명 사용자 ID가 없으면 새로 생성합니다.
            anonymousUserId = UUID.randomUUID().toString();
            // 새 쿠키를 생성하여 응답에 추가합니다.
            CookieUtil.createCookie(response, "anonymousUserId", anonymousUserId, 60 * 60 * 24 * 30); // 30일 동안 유효
        }

        // 오늘 날짜와 익명 사용자 ID로 기록 조회
        Optional<Hit> optionalHit = hitRepository.findByDateAndAnonymousUserId(today, anonymousUserId);
        Hit hit;

        if (optionalHit.isPresent()) {
            // 이미 오늘의 익명 방문 기록이 존재하면 카운트를 증가시킵니다.
            hit = optionalHit.get();
            hit.setHitCount(hit.getHitCount() + 1);
        } else {
            // 새로운 익명 방문 기록 생성
            hit = new Hit();
            hit.setDate(today);
            hit.setHitCount(1);
            hit.setAnonymousUserId(anonymousUserId);
        }

        hitRepository.save(hit);
    }

    public void mergeAnonymousVisitToUser(HttpServletRequest request, HttpServletResponse response, Long userId) {
        LocalDate today = LocalDate.now();

        // 쿠키에서 익명 사용자 ID 가져오기
        Optional<Cookie> optionalCookie = CookieUtil.getCookie(request, "anonymousUserId");
        if (optionalCookie.isPresent()) {
            String anonymousUserId = optionalCookie.get().getValue();

            // 익명 방문 기록 조회
            Optional<Hit> optionalAnonymousHit = hitRepository.findByDateAndAnonymousUserId(today, anonymousUserId);
            if (optionalAnonymousHit.isPresent()) {
                Hit anonymousHit = optionalAnonymousHit.get();

                // 로그인한 사용자에 대한 오늘의 기록이 있는지 확인
                Optional<Hit> optionalUserHit = hitRepository.findByDateAndUserId(today, userId);

                if (optionalUserHit.isPresent()) {
                    // 로그인 사용자에 대한 오늘의 기록이 이미 있으면 방문 수를 합산합니다.
                    Hit userHit = optionalUserHit.get();
                    userHit.setHitCount(userHit.getHitCount() + anonymousHit.getHitCount());

                    // 익명 기록 삭제
                    hitRepository.delete(anonymousHit);

                    // 회원 기록 업데이트
                    hitRepository.save(userHit);
                } else {
                    // 로그인한 사용자에 대한 기록이 없으면 익명 기록을 로그인 사용자로 업데이트
                    anonymousHit.setUserId(userId);
                    anonymousHit.setAnonymousUserId(null); // 익명 사용자 ID는 제거
                    hitRepository.save(anonymousHit);
                }
            }

            // 익명 사용자 ID 쿠키 삭제 (이미 회원으로 통합되었으므로 필요 없음)
            CookieUtil.deleteCookie(response, "anonymousUserId");
        }
    }

}
