package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.user.dto.HitListDTO;
import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

public interface DashBoardControllerDocs {
    @Operation(summary = "상점 접속을 기록합니다.")
    @ApiResponse(responseCode = "200", description = "상점 접속 기록 성공")
    public SuccessResponse<Boolean> recordHits(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "상점 접속 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상점 접속 목록 조회 성공")
    public SuccessResponse<HitListDTO> getHits(@LoginInfo Long id,
                                               @RequestParam(value = "filteredBy", required = false) @Parameter(description = "필터",
                                                         examples =
                                                                 {@ExampleObject(name = "일별", summary = "일별 조회", value = "daily"),
                                                                         @ExampleObject(name = "월별", summary = "월별 조회", value = "monthly"),
                                                                         @ExampleObject(name = "요일별", summary = "요일별 조회", value = "weekly"),
                                                                         @ExampleObject(name = "업체별", summary = "업체별 조회", value = "company")}) String filteredBy,
                                               @RequestParam(value = "year") int year, @RequestParam(value = "month") int month);

}
