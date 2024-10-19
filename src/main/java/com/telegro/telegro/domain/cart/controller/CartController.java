package com.telegro.telegro.domain.cart.controller;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.cart.service.CartService;
import com.telegro.telegro.global.apiPayLoad.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController implements CartControllerDocs {
    private final CartService cartService;

    @PostMapping("/{productId}")
    public SuccessResponse<CreatedCartDTO> addCartItem(Long id, Long productId, CartRequestDTO request) {
        return SuccessResponse.of(cartService.addCartItem(id, productId, request));
    }
}
