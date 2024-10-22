package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.company.dto.request.CompanySignUpDTO;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.company.service.CompanyService;
import com.telegro.telegro.domain.user.dto.request.UserRequestDTO;
import com.telegro.telegro.domain.user.dto.response.UserDetailDTO;
import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.domain.user.service.UserService;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import com.telegro.telegro.global.auth.annotation.LoginInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs{
    private final UserService userService;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public SuccessResponse<UserListDTO> getUsers(Long id, Role filteredBy, int page, int size) {
        return SuccessResponse.of(userService.getUsers(id, filteredBy, page, size));
    }

    @GetMapping("/{userId}")
    public SuccessResponse<?> getUserDetail(Long id, Long userId) {

        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!currentUser.getRole().equals(Role.ADMIN)){
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(companyRepository.findByUserId(user.getId()).isPresent()){
            return SuccessResponse.of(companyService.getCompanyDetail(userId));
        }

        return SuccessResponse.of(userService.getUserDetail(user));
    }

    @DeleteMapping("/{userId}")
    public SuccessResponse<Boolean> deleteUser(Long id, Long userId) {
        userService.deleteUser(id, userId);
        return SuccessResponse.of();
    }

    @PatchMapping("/{userId}")
    @Transactional
    public SuccessResponse<Long> updateUser(@LoginInfo Long id, @PathVariable Long userId, @RequestBody UserRequestDTO requestDTO) {

        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if (!Role.ADMIN.equals(currentUser.getRole())) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Long updatedUserId = userService.updateUser(userId, requestDTO.getUser());

        if (requestDTO.getCompany() != null) {
            companyRepository.findByUserId(userId)
                    .ifPresentOrElse(
                            existingCompany -> companyService.updateCompany(userId, requestDTO.getCompany()),
                            () -> companyService.createCompany(requestDTO.getUser().getUserId(), requestDTO.getCompany())
                    );
        }

        return SuccessResponse.of(updatedUserId);
    }

}
