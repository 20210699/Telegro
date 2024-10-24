package com.telegro.telegro.domain.user.service;

import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.company.service.CompanyService;
import com.telegro.telegro.domain.user.dto.HitListDTO;
import com.telegro.telegro.domain.user.dto.hitDTO;
import com.telegro.telegro.domain.user.dto.response.*;
import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import com.telegro.telegro.domain.user.entity.Hit;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.DeliveryAddressRepository;
import com.telegro.telegro.domain.user.repository.HitRepository;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.auth.dto.request.LoginRequestDto;
import com.telegro.telegro.global.auth.dto.response.SignUpUserInfoDto;
import com.telegro.telegro.global.common.CookieUtil;
import com.telegro.telegro.global.common.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final RedisUtil redisUtil;
    private final HitRepository hitRepository;

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
                .role(signUpUserInfoDto.getRole() != null ? signUpUserInfoDto.getRole() : Role.MEMBER)
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
            Optional<Company> company = companyRepository.findByUserId(userId);
            if(company.isPresent()){
                companyService.deleteCompany(userId);
            }
        }

        userRepository.deleteById(userId);
    }

    @Transactional
    public Long updateUser(Long userId, User request) {
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if (request.getUsername() != null) {
            userToUpdate.setUsername(request.getUsername());
        }
        if (request.getUserId() != null) {
            userToUpdate.setUserId(request.getUserId());
        }
        if (request.getPassword() != null) {
            userToUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            userToUpdate.setRole(request.getRole());
        }
        if (request.getPhone() != null) {
            userToUpdate.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            userToUpdate.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            userToUpdate.setAddress(request.getAddress());
        }
        if (request.getAddressDetail() != null) {
            userToUpdate.setAddressDetail(request.getAddressDetail());
        }
        if (request.getZipCode() != null) {
            userToUpdate.setZipCode(request.getZipCode());
        }

        return userRepository.save(userToUpdate).getId();
    }

    public UserInfoDTO getMyPage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        String key = getDefaultAddressKey(user.getId());
        Long defaultAddressId = Optional.ofNullable(redisUtil.getData(key))
                .map(Long::valueOf)
                .orElse(null);

        List<DeliveryAddressDetailDTO> addressDTOs = user.getDeliveryAddresses().stream()
                .map(deliveryAddress ->
                        DeliveryAddressDetailDTO.of(deliveryAddress,
                                Optional.ofNullable(defaultAddressId)
                                        .map(addressId -> addressId.equals(deliveryAddress.getId())).orElse(false)))
                .toList();

        return UserInfoDTO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .userId(user.getUserId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .addressList(addressDTOs)
                .build();
    }

    public CreateAddressDTO addDeliveryAddress(Long id, DeliveryAddress request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        DeliveryAddress address = DeliveryAddress.builder()
                .name(request.getName())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .zipcode(request.getZipcode())
                .user(user)
                .build();

        DeliveryAddress savedAddress = deliveryAddressRepository.save(address);

        return CreateAddressDTO.builder().id(savedAddress.getId()).build();
    }

    public void deleteDeliveryAddress(Long id, Long addressId) {
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!deliveryAddress.getUser().getUserId().equals(currentUser.getUserId())){
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        deliveryAddressRepository.delete(deliveryAddress);

    }

    public CreateAddressDTO updateDeliveryAddress(Long id, Long addressId, DeliveryAddress request) {
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));


        DeliveryAddress existingAddress = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));


        if (!existingAddress.getUser().getUserId().equals(currentUser.getUserId())) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }
        if (request.getName() != null) {
            existingAddress.setName(request.getName());
        }
        if (request.getAddress() != null) {
            existingAddress.setAddress(request.getAddress());
        }
        if (request.getAddressDetail() != null) {
            existingAddress.setAddressDetail(request.getAddressDetail());
        }
        if (request.getZipcode() != null) {
            existingAddress.setZipcode(request.getZipcode());
        }

        DeliveryAddress updatedAddress = deliveryAddressRepository.save(existingAddress);

        return CreateAddressDTO.builder().id(updatedAddress.getId()).build();
    }

    public void setDefaultDeliveryAddress(Long id, Long addressId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        deliveryAddressRepository.findById(addressId).orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        String key = getDefaultAddressKey(user.getId());
        redisUtil.setData(key, addressId.toString());

        log.info("사용자 {}의 기본 배송지가 배송지 ID {}로 설정되었습니다.", user.getUsername(), addressId);
    }

    private String getDefaultAddressKey(Long userId) {
        return "default_address_id_" + userId;
    }

}
