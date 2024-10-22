package com.telegro.telegro.domain.product.service;

import com.telegro.telegro.domain.product.dto.request.ProductRequestDTO;
import com.telegro.telegro.domain.product.dto.response.CreatedProductDTO;
import com.telegro.telegro.domain.product.dto.response.ProductDetailResponseDTO;
import com.telegro.telegro.domain.product.dto.response.ProductListDTO;
import com.telegro.telegro.domain.product.dto.response.ProductResponseDTO;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.product.entity.enums.Category;
import com.telegro.telegro.domain.product.repository.ProductRepository;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CreatedProductDTO createProduct(Long id, ProductRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Product product = Product.builder()
                .productModel(request.productModel())
                .productName(request.productName())
                .options(request.options())
                .category(request.category())
                .content(request.content())
                .priceBussiness(request.priceBussiness())
                .priceBest(request.priceBest())
                .priceDealer(request.priceDealer())
                .priceCustomer(request.priceCustomer())
                .pictures(request.pictures())
                .build();
        Product savedProduct = productRepository.save(product);

        // 대표 이미지가 없으면 등록한 사진 중 첫번째가 대표 이미지가 됨
        if (!request.pictures().isEmpty()) {
            if(request.coverImage() !=null){
                savedProduct.setCoverImage(request.coverImage());
            } else {
                savedProduct.setCoverImage(request.pictures().get(0));
            }
        } else {
            savedProduct.setCoverImage(null);
        }

        return CreatedProductDTO.builder()
                .id(savedProduct.getId()).build();
    }

    @Transactional
    public ProductListDTO getProducts(Long id, Category category, int page, int size) {

        final User user;
        if (id != null) {
            user = userRepository.findById(id).orElse(null);
        } else {
            user = null;
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategory(category, pageRequest);

        if (products.isEmpty()) {
            throw CustomException.of(Error.NOT_FOUND_ERROR);
        }

        // Product를 ProductResponseDTO로 변환
        List<ProductResponseDTO> productDTOs = products.stream()
                .map(product -> {
                    String price = selectPriceByUserRole(product, user);
                    return ProductResponseDTO.of(product, price);
                })
                .toList();

        return ProductListDTO.builder()
                .isLast(products.isLast())
                .totalElement(products.getTotalElements())
                .totalPage(products.getTotalPages())
                .products(productDTOs)
                .build();
    }

    @Transactional
    public ProductDetailResponseDTO getProductDetail(Long id, Long productId) {
        final User user;
        if (id != null) {
            user = userRepository.findById(id).orElse(null);
        } else {
            user = null;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        String price = selectPriceByUserRole(product, user);

        return ProductDetailResponseDTO.builder()
                .productModel(product.getProductModel())
                .productName(product.getProductName())
                .options(product.getOptions())
                .category(product.getCategory())
                .content(product.getContent())
                .price(price)
                .priceBussiness(product.getPriceBussiness())
                .priceBest(product.getPriceBest())
                .priceDealer(product.getPriceDealer())
                .priceCustomer(product.getPriceCustomer())
                .pictures(product.getPictures())
                .content(product.getContent())
                .coverImage(product.getCoverImage())
                .build();
    }

    @Transactional
    public void deleteProduct(Long id, Long productId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        if(!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }
        productRepository.deleteById(productId);
    }

    @Transactional
    public ProductDetailResponseDTO updateProduct(Long id, Long productId, Map<String, Object> request) {
        // 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        // 권한 확인 (ADMIN만 수정 가능)
        if (!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        // 업데이트할 필드를 적용
        request.forEach((key, value) -> {
            switch (key) {
                case "productModel":
                    product.setProductModel((String) value);
                    break;
                case "productName":
                    product.setProductName((String) value);
                    break;
                case "options":
                    product.setOptions((List<String>) value);
                    break;
                case "category":
                    product.setCategory(Category.valueOf((String) value));
                    break;
                case "content":
                    product.setContent((String) value);
                    break;
                case "priceBussiness":
                    product.setPriceBussiness((String) value);
                    break;
                case "priceBest":
                    product.setPriceBest((String) value);
                    break;
                case "priceDealer":
                    product.setPriceDealer((String) value);
                    break;
                case "priceCustomer":
                    product.setPriceCustomer((String) value);
                    break;
                case "pictures":
                    product.setPictures((List<String>) value);
                    break;
                case "coverImage":
                    product.setCoverImage((String) value);
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 필드: " + key);
            }
        });

        // 변경 사항 저장
        Product updatedProduct = productRepository.save(product);

        // 업데이트된 데이터를 DTO로 변환하여 반환
        return ProductDetailResponseDTO.builder()
                .productModel(updatedProduct.getProductModel())
                .productName(updatedProduct.getProductName())
                .options(updatedProduct.getOptions())
                .category(updatedProduct.getCategory())
                .content(updatedProduct.getContent())
                .priceBussiness(updatedProduct.getPriceBussiness())
                .priceBest(updatedProduct.getPriceBest())
                .priceDealer(updatedProduct.getPriceDealer())
                .priceCustomer(updatedProduct.getPriceCustomer())
                .pictures(updatedProduct.getPictures())
                .coverImage(updatedProduct.getCoverImage())
                .build();
    }


    private String selectPriceByUserRole(Product product, User user) {
        if (user == null) {
            // 로그인하지 않은 사용자는 'Customer' 가격을 보여줌
            return product.getPriceCustomer();
        }

        Role role = user.getRole();

        switch (role) {
            case BUSINESS:
                return product.getPriceBussiness();
            case BEST:
                return product.getPriceBest();
            case DEALER:
                return product.getPriceDealer();
            case MEMBER:
                return product.getPriceCustomer();
            case ADMIN:
                return null;
            default:
                throw new IllegalArgumentException("알 수 없는 사용자 역할입니다: " + role);
        }
    }

}
