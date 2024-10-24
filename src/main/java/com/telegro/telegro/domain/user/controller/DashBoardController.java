package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.user.dto.HitListDTO;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.service.UserService;
import com.telegro.telegro.domain.user.service.VisitService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DashBoardController implements DashBoardControllerDocs{
    private final VisitService visitService;

    @PostMapping("/hits")
    public SuccessResponse<Boolean> recordHits(HttpServletRequest request, HttpServletResponse response) {
        visitService.recordAnonymousVisit(request, response);
        return SuccessResponse.of();
    }

    @GetMapping("/api/hits")
    public SuccessResponse<HitListDTO> getHits(Long id, String filteredBy, int year, int month) {
//        return SuccessResponse.of(visitService.getHits(id, filteredBy, year, month));
        return null;
    }
}
