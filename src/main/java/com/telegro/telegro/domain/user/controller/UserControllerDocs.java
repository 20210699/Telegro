package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.company.dto.request.CompanySignUpDTO;
import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserControllerDocs {
    @Operation(summary = "회원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    public SuccessResponse<UserListDTO> getUsers(@LoginInfo Long id, @RequestParam(value = "page") int page, @RequestParam(value = "size") int size);

//    @Operation(summary = "회원 상세 정보를 조회합니다.")
//    @ApiResponse(responseCode = "200", description = "회원 상세 정보 조회 성공")
//    public SuccessResponse<UserDetailDTO> getUserDetail();

//    @Operation(summary = "회원 정보를 삭제합니다.")
//    @ApiResponse(responseCode = "200", description = "회원 정보 삭제 성공")
//    public SuccessResponse<Boolean> deleteUser(@LoginInfo Long id);
//
//    @Operation(summary = "회원 정보를 수정합니다.")
//    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
//    public SuccessResponse<UserDetailDTO> updateUser(@LoginInfo Long id);
}
