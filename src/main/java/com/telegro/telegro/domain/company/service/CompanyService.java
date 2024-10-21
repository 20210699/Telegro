package com.telegro.telegro.domain.company.service;

import com.telegro.telegro.domain.company.dto.request.CompanyRequestDTO;
import com.telegro.telegro.domain.company.dto.response.CompanyDetailDTO;
import com.telegro.telegro.domain.company.entity.Company;
import com.telegro.telegro.domain.company.repository.CompanyRepository;
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
    public void createCompany(CompanyRequestDTO companyRequestDTO){
        User user = userRepository.findByUserId(companyRequestDTO.userId());
        if (user == null) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        Company existCompany = companyRepository.findByCompanyName(companyRequestDTO.companyName());
        if(existCompany != null){
            throw CustomException.of(Error.COMPANY_NAME_ALREADY_USED_ERROR);
        }

        Company company = Company.builder()
                .managerName(companyRequestDTO.managerName())
                .managerPhone(companyRequestDTO.managerPhone())
                .companyName(companyRequestDTO.companyName())
                .companyNumber(companyRequestDTO.companyNumber())
                .companyType(companyRequestDTO.companyType())
                .companyItem(companyRequestDTO.companyItem())
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
                .id(company.getId())
                .role(company.getUser().getRole())
                .userid(company.getUser().getUserId())
                .username(company.getUser().getUsername())
                .password(company.getUser().getPassword())
                .phone(company.getUser().getPhone())
                .email(company.getUser().getEmail())
                .address(company.getUser().getAddress())
                .addressDetail(company.getUser().getAddressDetail())
                .zipCode(company.getUser().getZipCode())
                .managerName(company.getManagerName())
                .managerPhone(company.getManagerPhone())
                .companyName(company.getCompanyName())
                .companyNumber(company.getCompanyNumber())
                .companyType(company.getCompanyType())
                .companyItem(company.getCompanyItem())
                .build();
    }

    @Transactional
    public void updateCompany(CompanyRequestDTO companyRequestDTO){

    }

    @Transactional
    public void deleteCompany(Long companyId){
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (!optionalCompany.isPresent()) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        try {
            companyRepository.deleteById(companyId);
        } catch (Exception e) {
            log.error("Exception while deleting company: {}", e.getMessage());
            throw new RuntimeException("fail to delete company");
        }
    }
}
