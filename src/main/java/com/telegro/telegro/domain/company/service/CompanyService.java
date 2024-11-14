package com.telegro.telegro.domain.company.service;

import com.telegro.telegro.domain.company.dto.request.CompanyRequestDTO;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
import com.telegro.telegro.domain.user.dto.request.UserRequestDTO;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public void createCompany(String userId, Company companyRequestDTO) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        Company existCompany = companyRepository.findByCompanyName(companyRequestDTO.getCompanyName());
        if(existCompany != null){
            throw CustomException.of(Error.COMPANY_NAME_ALREADY_USED_ERROR);
        }

        Company company = Company.builder()
                .managerName(companyRequestDTO.getManagerName())
                .managerPhone(companyRequestDTO.getManagerPhone())
                .companyName(companyRequestDTO.getCompanyName())
                .companyNumber(companyRequestDTO.getCompanyNumber())
                .companyType(companyRequestDTO.getCompanyType())
                .companyItem(companyRequestDTO.getCompanyItem())
                .companyDescription(companyRequestDTO.getCompanyDescription())
                .user(user)
                .build();
        try {
            companyRepository.save(company);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("try to save duplicated company");
        }
    }

    @Transactional
    public CompanyDetailDTO getCompanyDetail(Long id){

        Company company = companyRepository.findByUserId(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        return CompanyDetailDTO.builder()
                .id(company.getUser().getId())
                .role(company.getUser().getRole())
                .userid(company.getUser().getUserId())
                .username(company.getUser().getUsername())
                .phone(company.getUser().getPhone())
                .email(company.getUser().getEmail())
                .address(company.getUser().getAddress())
                .addressDetail(company.getUser().getAddressDetail())
                .zipCode(company.getUser().getZipCode())
                .point(company.getUser().getPoint())
                .managerName(company.getManagerName())
                .managerPhone(company.getManagerPhone())
                .companyName(company.getCompanyName())
                .companyNumber(company.getCompanyNumber())
                .companyType(company.getCompanyType())
                .companyItem(company.getCompanyItem())
                .companyDescription(company.getCompanyDescription())
                .build();
    }

    @Transactional
    public void deleteCompany(Long userId){
        Optional<Company> optionalCompany = companyRepository.findByUserId(userId);
        if (optionalCompany.isEmpty()) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        try {
            companyRepository.deleteById(optionalCompany.get().getId());
        } catch (Exception e) {
            log.error("Exception while deleting company: {}", e.getMessage());
            throw new RuntimeException("fail to delete company");
        }
    }



    @Transactional
    public void updateCompany(Long userId, Company companyRequest){
        Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if (companyRequest.getManagerName() != null) {
            company.setManagerName(companyRequest.getManagerName());
        }
        if (companyRequest.getManagerPhone() != null) {
            company.setManagerPhone(companyRequest.getManagerPhone());
        }
        if (companyRequest.getCompanyName() != null) {
            company.setCompanyName(companyRequest.getCompanyName());
        }
        if (companyRequest.getCompanyNumber() != null) {
            company.setCompanyNumber(companyRequest.getCompanyNumber());
        }
        if (companyRequest.getCompanyType() != null) {
            company.setCompanyType(companyRequest.getCompanyType());
        }
        if (companyRequest.getCompanyItem() != null) {
            company.setCompanyItem(companyRequest.getCompanyItem());
        }
        if (companyRequest.getCompanyDescription() != null) {
            company.setCompanyDescription(companyRequest.getCompanyDescription());
        }
        // 변경된 회사 정보 저장
        companyRepository.save(company);
    }

}
