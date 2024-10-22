package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.company.dto.request.CompanySignUpDTO;
import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.user.dto.request.UserRequestDTO;
import com.telegro.telegro.domain.user.dto.response.CreateAddressDTO;
import com.telegro.telegro.domain.user.dto.response.UserDetailDTO;
import com.telegro.telegro.domain.user.dto.response.UserInfoDTO;
import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserControllerDocs {
    @Operation(summary = "회원 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    public SuccessResponse<UserListDTO> getUsers(@LoginInfo Long id,
                                                 @RequestParam(value = "filteredBy", required = false) @Parameter(description = "단가 적용 필터링",
                                                         examples =
                                                                 {@ExampleObject(name = "일반 멤버", summary = "일반 멤버 조회", value = "MEMBER"),
                                                                         @ExampleObject(name = "Business", summary = "Business 조회", value = "BUSINESS"),
                                                                         @ExampleObject(name = "Best", summary = "Best 조회", value = "BEST"),
                                                                         @ExampleObject(name = "Dealer", summary = "Dealer 조회", value = "DEALER")}) Role filteredBy,
                                                 @RequestParam(value = "page") int page, @RequestParam(value = "size") int size);

    @Operation(summary = "회원 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 상세 정보 조회 성공")
    public SuccessResponse<?> getUserDetail(@LoginInfo Long id, @PathVariable Long userId);

    @Operation(summary = "회원 정보를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 삭제 성공")
    public SuccessResponse<Boolean> deleteUser(@LoginInfo Long id, @PathVariable Long userId);

    @Operation(summary = "회원 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
    public SuccessResponse<Long> updateUser(@LoginInfo Long id, @PathVariable Long userId, @RequestBody UserRequestDTO request);

    @Operation(summary = "마이페이지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공")
    public SuccessResponse<UserInfoDTO> getMyPage(@LoginInfo Long id);

    @Operation(summary = "배송지를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 추가 성공")
    public SuccessResponse<CreateAddressDTO> addDeliveryAddress(@LoginInfo Long id, @RequestBody DeliveryAddress deliveryAddress);

    @Operation(summary = "배송지를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "배송지 추가 성공")
    public SuccessResponse<Boolean> addDeliveryAddress(@LoginInfo Long id, @PathVariable Long addressId);

//    @Operation(summary = "배송지를 추가합니다.")
//    @ApiResponse(responseCode = "200", description = "배송지 추가 성공")
//    public SuccessResponse<CreateAddressDTO> addDeliveryAddress(@LoginInfo Long id, @RequestBody DeliveryAddress deliveryAddress);
}
