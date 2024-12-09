package com.telegro.telegro.domain.cart.service;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CartListDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
import com.telegro.telegro.domain.cart.entity.enums.CartStatus;
import com.telegro.telegro.domain.cart.repository.CartRepository;
import com.telegro.telegro.domain.product.entity.Product;
import com.telegro.telegro.domain.product.repository.ProductRepository;
import com.telegro.telegro.domain.product.service.ProductService;
import com.telegro.telegro.domain.user.entity.User;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;

    @Transactional
    public CreatedCartDTO addCartItem(Long userId, Long productId, CartRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.PRODUCT_NOT_FOUND));

        List<Cart> carts = cartRepository.findByUserAndProduct(user, product);

        BigDecimal productPrice = productService.selectPriceByUserRole(product, user);

        Cart cart = carts.stream()
                .filter(existingCart -> Objects.equals(existingCart.getSelectOption(), request.selectOption()) &&
                        Objects.equals(existingCart.getInputOption(), request.inputOption()) &&
                        CartStatus.IN_CART.equals(existingCart.getCartStatus()))
                .findFirst()
                .map(existingCart -> updateExistingCart(existingCart, request))
                .orElseGet(() -> createNewCart(user, product, request, productPrice));

        Cart savedCart = cartRepository.save(cart);

        return CreatedCartDTO.builder()
                .id(savedCart.getId()).build();
    }

    private Cart updateExistingCart(Cart existingCart, CartRequestDTO request) {
        existingCart.setQuantity(existingCart.getQuantity() + request.quantity());
        return existingCart;
    }

    private Cart createNewCart(User user, Product product, CartRequestDTO request, BigDecimal productPrice) {
        return Cart.builder()
                .user(user)
                .product(product)
                .quantity(request.quantity())
                .selectOption(request.selectOption())
                .inputOption(request.inputOption())
                .price(productPrice)
                .totalPrice(productPrice.multiply(BigDecimal.valueOf(request.quantity())))
                .cartStatus(CartStatus.IN_CART)
                .build();
    }

    @Transactional
    public CartListDTO getCartItems(Long id, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Cart> carts = cartRepository.findAllInCartByUser(user, pageRequest);
        boolean isLast = carts.isLast();
        int totalPage = carts.getTotalPages();
        long totalElement = carts.getTotalElements();

        List<CartResponseDTO> cartDTOs = carts.getContent().stream()
                .map(CartResponseDTO::of).toList();

        BigDecimal totalPrice = cartDTOs.stream()
                .map(cartDTO -> cartDTO.productPrice().multiply(BigDecimal.valueOf(cartDTO.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartListDTO.builder()
                .isLast(isLast)
                .totalPage(totalPage)
                .totalElement(totalElement)
                .totalPrice(totalPrice)
                .carts(cartDTOs)
                .build();
    }

    @Transactional
    public void deleteCartItem(Long id, Long cartId) {
        cartRepository.deleteByIdAndUserId(cartId, id);
    }

    @Transactional
    public CreatedCartDTO updateCartItem(Long id, Long cartId, CartRequestDTO request) {
        Cart cart = cartRepository.findByIdAndUserId(cartId, id)
                .orElseThrow(() -> CustomException.of(Error.CART_NOT_FOUND));

        List<Cart> existingCarts = cartRepository.findByUserAndProduct(cart.getUser(), cart.getProduct()).stream()
                .filter(c -> !c.getId().equals(cart.getId()) && c.getSelectOption().equals(request.selectOption())
                        && c.getInputOption().equals(request.inputOption()))
                .toList();

        if (!existingCarts.isEmpty()) {
            Cart existingCart = existingCarts.get(0);
            existingCart.setQuantity(existingCart.getQuantity() + request.quantity());

            // Update totalPrice for merged cart item
            existingCart.setTotalPrice(existingCart.getPrice().multiply(BigDecimal.valueOf(existingCart.getQuantity())));

            cartRepository.delete(cart);
            Cart updatedCart = cartRepository.save(existingCart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        } else {
            cart.setSelectOption(request.selectOption());
            cart.setInputOption(request.inputOption());
            cart.setQuantity(request.quantity());

            // Update totalPrice for updated cart item
            cart.setTotalPrice(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));

            Cart updatedCart = cartRepository.save(cart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        }
    }


}
