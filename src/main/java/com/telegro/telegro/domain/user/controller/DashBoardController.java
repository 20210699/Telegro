package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.user.dto.HitListDTO;
import com.telegro.telegro.domain.user.dto.hitDTO;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.domain.user.service.VisitService;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DashBoardController implements DashBoardControllerDocs{
    private final VisitService visitService;
    private final UserRepository userRepository;

    @PostMapping("/hits")
    public SuccessResponse<Boolean> recordHits(HttpServletRequest request, HttpServletResponse response) {
        visitService.recordAnonymousVisit(request, response);
        return SuccessResponse.of();
    }

    @GetMapping("/api/hits")
    public SuccessResponse<HitListDTO> getHits(Long id, String filteredBy, Integer year, Integer month) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().equals(Role.ADMIN)){
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        List<hitDTO> hitList = switch (filteredBy != null ? filteredBy : "daily") {
            case "daily" -> visitService.getDailyHits(year, month);
            case "monthly" -> visitService.getMonthlyHits(year);
            case "weekly" -> visitService.getWeeklyHits(year, month);
            case "company" -> visitService.getCompanyHits();
            default -> throw new IllegalArgumentException("잘못된 필터 값입니다.");
        };

        // 평균 및 총 계 계산
        double totalHit = hitList.stream().mapToDouble(hitDTO::hit).sum();
        double averageHit = hitList.isEmpty() ? 0 : totalHit / hitList.size();
        double overAllTotalHit = visitService.getOverallTotalHits();

        // 조회한 데이터를 DTO로 변환
        HitListDTO hitListDTO = HitListDTO.builder()
                .hits(hitList)
                .averageHit(averageHit)
                .totalHit(totalHit)
                .overAllTotalHit(overAllTotalHit)
                .build();

        return SuccessResponse.of(hitListDTO);
    }
}
