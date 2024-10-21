package com.telegro.telegro.domain.user.controller;

import com.telegro.telegro.domain.user.dto.response.UserListDTO;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.service.UserService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs{
    private final UserService userService;

    @GetMapping
    public SuccessResponse<UserListDTO> getUsers(Long id, Role filteredBy, int page, int size) {
        return SuccessResponse.of(userService.getUsers(id, filteredBy, page, size));
    }
}
