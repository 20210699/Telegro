package com.telegro.telegro.domain.cart.service;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.product.repository.ProductRepository;
import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.repository.UserRepository;
import com.telegro.telegro.global.apiPayLoad.exception.CustomException;
import com.telegro.telegro.global.apiPayLoad.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    @Transactional
    public CreatedCartDTO addCartItem(Long userId, Long productId, CartRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        List<Cart> carts = cartRepository.findByUserAndProduct(user, product);

        Cart cart = carts.stream()
                .filter(existingCart -> existingCart.getProductOption().equals(request.productOption()))
                .findFirst()
                .map(existingCart -> updateExistingCart(existingCart, request))
                .orElseGet(() -> createNewCart(user, product, request));

        Cart savedCart = cartRepository.save(cart);

        return CreatedCartDTO.builder()
                .id(savedCart.getId()).build();
    }

    private Cart updateExistingCart(Cart existingCart, CartRequestDTO request) {
        existingCart.setQuantity(existingCart.getQuantity() + request.quantity());
        existingCart.setProductPrice(existingCart.getProductPrice() + request.productPrice() * request.quantity());
        return existingCart;
    }

    private Cart createNewCart(User user, Product product, CartRequestDTO request) {
        return Cart.builder()
                .user(user)
                .product(product)
                .quantity(request.quantity())
                .productOption(request.productOption())
                .productPrice(request.productPrice() * request.quantity())
                .build();
    }

}
