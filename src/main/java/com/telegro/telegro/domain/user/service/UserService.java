package com.telegro.telegro.domain.user.service;

import com.telegro.telegro.domain.company.controller.CompanyController;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.company.service.CompanyService;
import com.telegro.telegro.domain.user.dto.response.UserDTO;
import com.telegro.telegro.domain.user.dto.response.UserDetailDTO;
import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.auth.dto.request.LoginRequestDto;
import com.telegro.telegro.global.auth.dto.response.SignUpUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    public void signUp(SignUpUserInfoDto signUpUserInfoDto) {
        User existUser = userRepository
                .findByUserId(signUpUserInfoDto.getUserid());

        if(existUser != null){
            throw CustomException.of(Error.NICKNAME_ALREADY_USED_ERROR);
        }

        User user = User.builder()
                .userId(signUpUserInfoDto.getUserid())
                .username(signUpUserInfoDto.getUsername())
                .phone(signUpUserInfoDto.getPhone())
                .address(signUpUserInfoDto.getAddress())
                .addressDetail(signUpUserInfoDto.getAddressDetail())
                .zipCode(signUpUserInfoDto.getZipCode())
                .totalPrice(0L)
                .email(signUpUserInfoDto.getEmail())
                .role(Role.MEMBER)
                .password(passwordEncoder.encode(signUpUserInfoDto.getPassword()))
                .build();

        try {
            userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("try to save duplicated user");
        }
    }

    public Long getUserId(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByUserId(loginRequestDto.id());
        if (user == null) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        if (!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
            throw CustomException.of(Error.INVALID_ID_PASSWORD);
        }
        return user.getId();
    }

    @Transactional
    public UserListDTO getUsers(Long id, Role filteredBy, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().equals(Role.ADMIN)){
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users;
        if (filteredBy != null) {
            users = userRepository.findByRole(filteredBy, pageRequest);
        } else {
            users = userRepository.findByRoleNot(Role.ADMIN, pageRequest);
        }

        boolean isLast = users.isLast();
        int totalPage = users.getTotalPages();
        long totalElement = users.getTotalElements();

        List<UserDTO> userList = users.getContent().stream()
                .map(UserDTO::of)
                .collect(Collectors.toList());

        return UserListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .users(userList)
                .build();
    }

    @Transactional
    public UserDetailDTO getUserDetail(User user) {

        return UserDetailDTO.builder()
                .id(user.getId())
                .role(user.getRole())
                .userid(user.getUserId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .address(user.getAddress())
                .addressDetail(user.getAddressDetail())
                .zipCode(user.getZipCode())
                .build();
    }

    @Transactional
    public void deleteUser(Long id, Long userId) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!admin.getRole().equals(Role.ADMIN)){
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().equals(Role.ADMIN) && !user.getRole().equals(Role.MEMBER)){
            Company company = companyRepository.findByUserId(userId)
                    .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));
            companyService.deleteCompany(company.getId());
        }

        userRepository.deleteById(userId);
    }
}
