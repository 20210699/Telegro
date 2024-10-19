package com.telegro.telegro.domain.cart.controller;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CartListDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.cart.service.CartService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController implements CartControllerDocs {
    private final CartService cartService;

    @PostMapping("/{productId}")
    public SuccessResponse<CreatedCartDTO> addCartItem(Long id, Long productId, CartRequestDTO request) {
        return SuccessResponse.of(cartService.addCartItem(id, productId, request));
    }

    @GetMapping
    public SuccessResponse<CartListDTO> getCartItems(Long id, int page, int size) {
        return SuccessResponse.of(cartService.getCartItems(id, page, size));
    }

    @DeleteMapping("/{cartId}")
    public SuccessResponse<Boolean> deleteCartItem(Long id, Long cartId) {
        cartService.deleteCartItem(id, cartId);
        return SuccessResponse.of();
    }

}
