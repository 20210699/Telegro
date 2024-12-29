package com.telegro.telegro.domain.product.service;

import com.telegro.telegro.domain.cart.repository.CartRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Transactional
    public CreatedProductDTO createProduct(Long id, ProductRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        Product product = Product.builder()
                .productModel(request.productModel())
                .productName(request.productName())
                .options(request.options())
                .category(request.category())
                .content(request.content())
                .priceBussiness(BigDecimal.valueOf(Double.parseDouble(request.priceBussiness())))
                .priceBest(BigDecimal.valueOf(Double.parseDouble(request.priceBest())))
                .priceDealer(BigDecimal.valueOf(Double.parseDouble(request.priceDealer())))
                .priceCustomer(BigDecimal.valueOf(Double.parseDouble(request.priceCustomer())))
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

        // Product를 ProductResponseDTO로 변환
        List<ProductResponseDTO> productDTOs = products.stream()
                .map(product -> {
                    BigDecimal price = selectPriceByUserRole(product, user);
                    return ProductResponseDTO.of(product, price.toString());
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
                .orElseThrow(() -> CustomException.of(Error.PRODUCT_NOT_FOUND));

        BigDecimal price = selectPriceByUserRole(product, user);

        return ProductDetailResponseDTO.builder()
                .productModel(product.getProductModel())
                .productName(product.getProductName())
                .options(product.getOptions())
                .category(product.getCategory())
                .content(product.getContent())
                .price(price.toString())
                .priceBussiness(product.getPriceBussiness().toString())
                .priceBest(product.getPriceBest().toString())
                .priceDealer(product.getPriceDealer().toString())
                .priceCustomer(product.getPriceCustomer().toString())
                .pictures(product.getPictures())
                .content(product.getContent())
                .coverImage(product.getCoverImage())
                .build();
    }

    @Transactional
    public void deleteProduct(Long id, Long productId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if(!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }
        cartRepository.deleteByProductId(productId);

        productRepository.deleteById(productId);
    }

    @Transactional
    public ProductDetailResponseDTO updateProduct(Long id, Long productId, Map<String, Object> request) {
        // 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        // 권한 확인 (ADMIN만 수정 가능)
        if (!user.getRole().toString().equals("ADMIN")) {
            throw CustomException.of(Error.FORBIDDEN_ACTION_ERROR);
        }

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.PRODUCT_NOT_FOUND));

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
                    product.setPriceBussiness(BigDecimal.valueOf((Integer)value));
                    break;
                case "priceBest":
                    product.setPriceBest(BigDecimal.valueOf((Integer)value));
                    break;
                case "priceDealer":
                    product.setPriceDealer(BigDecimal.valueOf((Integer)value));
                    break;
                case "priceCustomer":
                    product.setPriceCustomer(BigDecimal.valueOf((Integer)value));
                    break;
                case "pictures":
                    product.setPictures((List<String>) value);
                    product.setCoverImage(product.getPictures().get(0));
                    break;
                case "coverImage":
                    product.setCoverImage((String) value);
                    break;
                default:
                    throw new IllegalArgumentException("알 수 없는 필드: " + key);
            }
        });

        Product updatedProduct = productRepository.save(product);

        return ProductDetailResponseDTO.builder()
                .productModel(updatedProduct.getProductModel())
                .productName(updatedProduct.getProductName())
                .options(updatedProduct.getOptions())
                .category(updatedProduct.getCategory())
                .content(updatedProduct.getContent())
                .priceBussiness(String.valueOf(updatedProduct.getPriceBussiness()))
                .priceBest(String.valueOf(updatedProduct.getPriceBest()))
                .priceDealer(String.valueOf(updatedProduct.getPriceDealer()))
                .priceCustomer(String.valueOf(updatedProduct.getPriceCustomer()))
                .pictures(updatedProduct.getPictures())
                .coverImage(updatedProduct.getCoverImage())
                .build();
    }


    public BigDecimal selectPriceByUserRole(Product product, User user) {
        if (user == null) {
            // 로그인하지 않은 사용자는 'Customer' 가격을 보여줌
            return product.getPriceCustomer();
        }

        Role role = user.getRole();

        return switch (role) {
            case BUSINESS -> product.getPriceBussiness();
            case BEST -> product.getPriceBest();
            case DEALER -> product.getPriceDealer();
            default -> product.getPriceCustomer();
        };
    }

}
