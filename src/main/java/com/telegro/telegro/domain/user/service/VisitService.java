package com.telegro.telegro.domain.user.service;

import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.user.dto.hitDTO;
import com.telegro.telegro.domain.user.entity.Hit;
import com.telegro.telegro.domain.user.repository.HitRepository;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.common.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

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
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public void mergeAnonymousVisitToUser(HttpServletRequest request, HttpServletResponse response, Long userId) {
        LocalDate today = LocalDate.now();
        Optional<Cookie> optionalCookie = CookieUtil.getCookie(request, "anonymousUserId");

        if (optionalCookie.isPresent()) {
            String anonymousUserId = optionalCookie.get().getValue();
            System.out.println("Anonymous User ID: " + anonymousUserId); // 로그 추가

            Optional<Hit> optionalAnonymousHit = hitRepository.findByDateAndAnonymousUserId(today, anonymousUserId);
            if (optionalAnonymousHit.isPresent()) {
                Hit anonymousHit = optionalAnonymousHit.get();
                Optional<Hit> optionalUserHit = hitRepository.findByDateAndUserId(today, userId);

                if (optionalUserHit.isPresent()) {
                    Hit userHit = optionalUserHit.get();
                    userHit.setHitCount(userHit.getHitCount() + anonymousHit.getHitCount());
                    hitRepository.delete(anonymousHit);
                    hitRepository.save(userHit);
                } else {
                    anonymousHit.setUserId(userId);
                    anonymousHit.setAnonymousUserId(null);
                    hitRepository.save(anonymousHit);
                }
            } else {
                System.out.println("No anonymous hit found for today and user ID: " + anonymousUserId); // 로그 추가
            }

            CookieUtil.deleteCookie(response, "anonymousUserId");
        } else {
            System.out.println("AnonymousUserId cookie not present"); // 로그 추가
        }
    }


    public List<hitDTO> getDailyHits(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Hit> hits = hitRepository.findByDateBetween(startDate, endDate);

        int totalMonthlyHits = getTotalHits(hits);

        // 날짜별로 접속량을 합산하여 Map 생성
        Map<LocalDate, Integer> hitMap = hits.stream()
                .collect(Collectors.toMap(
                        Hit::getDate,
                        Hit::getHitCount,
                        Integer::sum
                ));

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> hitDTO.builder()
                        .name(String.valueOf(date.getDayOfMonth()))
                        .hit(hitMap.getOrDefault(date, 0))
                        .percentage(calculatePercentage(hitMap.getOrDefault(date, 0), totalMonthlyHits))
                        .build())
                .collect(Collectors.toList());
    }

    public List<hitDTO> getMonthlyHits(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        List<Hit> yearlyHits = hitRepository.findByDateBetween(startOfYear, endOfYear);

        int totalYearlyHits = getTotalHits(yearlyHits);
        List<hitDTO> monthlyHits = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            List<Hit> monthlyData = hitRepository.findByDateBetween(startDate, endDate);
            int monthlyHitCount = getTotalHits(monthlyData);
            double percentage = calculatePercentage(monthlyHitCount, totalYearlyHits);

            monthlyHits.add(hitDTO.builder()
                    .name(month + "월")
                    .hit(monthlyHitCount)
                    .percentage(percentage)
                    .build());
        }

        return monthlyHits;
    }

    public List<hitDTO> getWeeklyHits(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Hit> hits = hitRepository.findByDateBetween(startDate, endDate);

        int totalMonthlyHits = getTotalHits(hits);
        Map<DayOfWeek, Integer> weeklyHitMap = initializeWeeklyMap();

        hits.forEach(hit -> {
            DayOfWeek dayOfWeek = hit.getDate().getDayOfWeek();
            weeklyHitMap.merge(dayOfWeek, hit.getHitCount(), Integer::sum);
        });

        return weeklyHitMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int dayHitCount = entry.getValue();
                    double percentage = calculatePercentage(dayHitCount, totalMonthlyHits);
                    return hitDTO.builder()
                            .name(entry.getKey().getDisplayName(TextStyle.FULL, Locale.getDefault()))
                            .hit(dayHitCount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<hitDTO> getCompanyHits() {
        List<Hit> hits = hitRepository.findAll();
        double totalHits = getOverallTotalHits();
        Map<String, Integer> companyHitMap = new HashMap<>();

        for (Hit hit : hits) {
            String displayName = getDisplayName(hit);
            companyHitMap.put(displayName, companyHitMap.getOrDefault(displayName, 0) + hit.getHitCount());
        }

        return companyHitMap.entrySet().stream()
                .map(entry -> {
                    int hitCount = entry.getValue();
                    double percentage = calculatePercentage(hitCount, (int) totalHits);
                    return hitDTO.builder()
                            .name(entry.getKey())
                            .hit(hitCount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private int getTotalHits(List<Hit> hits) {
        return hits.stream().mapToInt(Hit::getHitCount).sum();
    }

    private double calculatePercentage(int part, int total) {
        return total > 0 ? Math.round((part / (double) total) * 100 * 100) / 100.0 : 0.0;
    }

    private Map<DayOfWeek, Integer> initializeWeeklyMap() {
        return Arrays.stream(DayOfWeek.values())
                .collect(Collectors.toMap(day -> day, day -> 0));
    }

    private String getDisplayName(Hit hit) {
        if (hit.getUserId() == null || hit.getAnonymousUserId() != null) {
            return "비회원";
        }

        return companyRepository.findByUserId(hit.getUserId())
                .map(Company::getCompanyName)
                .orElseGet(() -> userRepository.findById(hit.getUserId()).isPresent() ? "일반 회원" : "알 수 없음");
    }

    public int getOverallTotalHits() {
        return hitRepository.findAll().stream()
                .mapToInt(Hit::getHitCount)
                .sum();
    }
}
