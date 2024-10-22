package com.telegro.telegro.domain.company.controller;

import com.telegro.telegro.domain.company.dto.request.CompanyRequestDTO;
import com.telegro.telegro.domain.company.dto.request.CompanySignUpDTO;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.domain.company.service.CompanyService;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.domain.user.service.UserService;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanyControllerDocs{

    // TODO : @PreAuthorize 사용하여 관리자 권한 구현 해보기
    private final CompanyService companyService;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    public SuccessResponse<?> createCompany(Long id, CompanySignUpDTO companySignUpDTO) {
        validateAdminAccess(id);
        userService.signUp(companySignUpDTO.getSignUpUserInfoDto());
        companyService.createCompany(companySignUpDTO.getSignUpUserInfoDto().getUserid(),companySignUpDTO.getCompany());
        return SuccessResponse.of();
    }

    @GetMapping
    public SuccessResponse<CompanyDetailDTO> getCompanyDetail(Long id) {
        return SuccessResponse.of(companyService.getCompanyDetail(id));
    }

    @DeleteMapping("/{userId}")
    public SuccessResponse<?> deleteCompany(Long id, Long userId) {
        validateAdminAccess(id);
        companyService.deleteCompany(userId);
        return SuccessResponse.of();
    }

    private void validateAdminAccess(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
        if (!user.getRole().equals(Role.ADMIN)) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }
    }
}
