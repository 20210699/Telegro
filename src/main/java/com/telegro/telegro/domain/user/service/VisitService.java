package com.telegro.telegro.domain.user.service;

import com.telegro.telegro.domain.user.dto.HitListDTO;
import com.telegro.telegro.domain.user.dto.hitDTO;
import com.telegro.telegro.domain.user.entity.Hit;
import com.telegro.telegro.domain.user.repository.HitRepository;
import com.telegro.telegro.global.common.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<hitDTO> getDailyHits(int year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 데이터베이스에서 해당 월의 데이터를 가져옵니다.
        List<Hit> hits = hitRepository.findByDateBetween(startDate, endDate);

        // 총 월 접속량 계산
        int totalMonthlyHits = hits.stream()
                .mapToInt(Hit::getHitCount)
                .sum();

        // 조회된 데이터를 날짜별로 매핑합니다. 중복 키가 있을 경우 hitCount 값을 합산합니다.
        Map<LocalDate, Integer> hitMap = hits.stream()
                .collect(Collectors.toMap(
                        Hit::getDate,
                        Hit::getHitCount,
                        Integer::sum  // 중복된 키가 있을 경우 hitCount를 합산하여 처리
                ));

        // 해당 월의 모든 날짜에 대해 hitDTO를 생성합니다.
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    int dailyHitCount = hitMap.getOrDefault(date, 0);
                    double percentage = totalMonthlyHits > 0
                            ? Math.round(dailyHitCount / (double) totalMonthlyHits * 100 * 100) / 100.0
                            : 0.0;

                    return hitDTO.builder()
                            .name(String.valueOf(date.getDayOfMonth()))
                            .hit(dailyHitCount) // 데이터가 없으면 0을 기본값으로 설정합니다.
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<hitDTO> getMonthlyHits(int year) {
        // 연도 전체의 데이터 가져오기
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        List<Hit> yearlyHits = hitRepository.findByDateBetween(startOfYear, endOfYear);

        // 연도의 총 접속량 계산
        int totalYearlyHits = yearlyHits.stream()
                .mapToInt(Hit::getHitCount)
                .sum();

        // 월별 데이터 및 비율 계산
        List<hitDTO> monthlyHits = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            // 해당 월의 데이터 가져오기
            List<Hit> monthlyData = hitRepository.findByDateBetween(startDate, endDate);

            // 월별 총 접속량 계산
            int monthlyHitCount = monthlyData.stream()
                    .mapToInt(Hit::getHitCount)
                    .sum();

            // 월별 접속량 비율 계산
            double percentage = totalYearlyHits > 0
                    ? (monthlyHitCount / (double) totalYearlyHits) * 100
                    : 0.0;

            // 월별 접속량 정보를 hitDTO로 추가
            monthlyHits.add(hitDTO.builder()
                    .name(month + "월")
                    .hit(monthlyHitCount)
                    .percentage(Math.round(percentage * 100) / 100.0) // 소수점 둘째 자리까지 반올림
                    .build());
        }

        return monthlyHits;
    }

    public List<hitDTO> getWeeklyHits(int year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 데이터베이스에서 해당 월의 데이터를 가져옵니다.
        List<Hit> hits = hitRepository.findByDateBetween(startDate, endDate);

        // 총 월 접속량 계산
        int totalMonthlyHits = hits.stream()
                .mapToInt(Hit::getHitCount)
                .sum();

        // 요일별 접속량을 저장할 맵 초기화
        Map<DayOfWeek, Integer> weeklyHitMap = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            weeklyHitMap.put(day, 0);
        }

        // 조회된 데이터를 요일별로 합산합니다.
        for (Hit hit : hits) {
            DayOfWeek dayOfWeek = hit.getDate().getDayOfWeek();
            weeklyHitMap.put(dayOfWeek, weeklyHitMap.get(dayOfWeek) + hit.getHitCount());
        }

        // 요일별 접속량 정보를 hitDTO 리스트로 변환하여 반환 (월요일부터 순차 정렬)
        return weeklyHitMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 요일을 월요일부터 일요일 순으로 정렬
                .map(entry -> {
                    int dayHitCount = entry.getValue();
                    double percentage = totalMonthlyHits > 0
                            ? Math.round(dayHitCount / (double) totalMonthlyHits * 100 * 100) / 100.0
                            : 0.0;

                    return hitDTO.builder()
                            .name(entry.getKey().getDisplayName(TextStyle.FULL, Locale.getDefault())) // 요일 이름
                            .hit(dayHitCount)
                            .percentage(percentage) // 요일별 접속량 비율 설정
                            .build();
                })
                .collect(Collectors.toList());
    }

}
