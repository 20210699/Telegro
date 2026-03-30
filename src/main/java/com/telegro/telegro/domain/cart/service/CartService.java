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
import com.telegro.telegro.global.apiPayLoad.response.CursorPagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        int quantity = existingCart.getQuantity() + request.quantity();
        existingCart.setQuantity(quantity);
        existingCart.setTotalPrice(existingCart.getPrice().multiply(BigDecimal.valueOf(quantity)));
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

    @Transactional(readOnly = true)
    public CartListDTO getCartItems(Long id, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.USER_NOT_FOUND));

        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }
        if (size < 1) {
            throw CustomException.of(Error.BAD_REQUEST_ERROR);
        }

        PageRequest pageRequest = PageRequest.of(0, size + 1, Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));

        List<Cart> carts = cartRepository.findAllInCartByUserWithCursor(user, cursorCreatedAt, cursorId, pageRequest);
        long totalElements = cartRepository.countByUserAndCartStatus(user, CartStatus.IN_CART);

        boolean isLast = carts.size() <= size;
        List<Cart> pagedCarts = isLast ? carts : new ArrayList<>(carts.subList(0, size));
        Cart nextCursorCart = isLast ? null : pagedCarts.get(pagedCarts.size() - 1);

        List<CartResponseDTO> cartDTOs = pagedCarts.stream()
                .map(CartResponseDTO::of).toList();

        BigDecimal totalPrice = cartDTOs.stream()
                .map(cartDTO -> cartDTO.productPrice().multiply(BigDecimal.valueOf(cartDTO.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartListDTO(
                totalPrice,
                CursorPagedResponse.of(
                        !isLast,
                        CursorPagedResponse.cursorOf(
                                nextCursorCart != null ? nextCursorCart.getId() : null,
                                nextCursorCart != null ? nextCursorCart.getCreatedAt() : null
                        ),
                        totalElements,
                        cartDTOs
                )
        );
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
                .filter(c -> !c.getId().equals(cart.getId())
                        && Objects.equals(c.getSelectOption(), request.selectOption())
                        && Objects.equals(c.getInputOption(), request.inputOption()))
                .toList();

        if (!existingCarts.isEmpty()) {
            Cart existingCart = existingCarts.get(0);
            existingCart.setQuantity(existingCart.getQuantity() + request.quantity());

            existingCart.setTotalPrice(existingCart.getPrice().multiply(BigDecimal.valueOf(existingCart.getQuantity())));

            cartRepository.delete(cart);
            Cart updatedCart = cartRepository.save(existingCart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        } else {
            cart.setSelectOption(request.selectOption() != null ? request.selectOption() : cart.getSelectOption());
            cart.setInputOption(request.inputOption() != null ? request.inputOption() : cart.getInputOption());
            cart.setQuantity(request.quantity());

            cart.setTotalPrice(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));

            Cart updatedCart = cartRepository.save(cart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        }
    }
}
