package com.telegro.telegro.domain.cart.service;

import com.telegro.telegro.domain.cart.dto.request.CartRequestDTO;
import com.telegro.telegro.domain.cart.dto.response.CartListDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.cart.dto.response.CreatedCartDTO;
import com.telegro.telegro.domain.cart.entity.Cart;
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

import java.util.List;

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
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        List<Cart> carts = cartRepository.findByUserAndProduct(user, product);

        Cart cart = carts.stream()
                .filter(existingCart -> existingCart.getSelectOption().equals(request.selectOption())
                && existingCart.getInputOption().equals(request.inputOption()))
                .findFirst()
                .map(existingCart -> updateExistingCart(existingCart, request))
                .orElseGet(() -> createNewCart(user, product, request));

        Cart savedCart = cartRepository.save(cart);

        return CreatedCartDTO.builder()
                .id(savedCart.getId()).build();
    }

    private Cart updateExistingCart(Cart existingCart, CartRequestDTO request) {
        existingCart.setQuantity(existingCart.getQuantity() + request.quantity());
        return existingCart;
    }

    private Cart createNewCart(User user, Product product, CartRequestDTO request) {
        return Cart.builder()
                .user(user)
                .product(product)
                .quantity(request.quantity())
                .selectOption(request.selectOption())
                .inputOption(request.inputOption())
                .build();
    }

    @Transactional
    public CartListDTO getCartItems(Long id, int page, int size) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Cart> carts = cartRepository.findAllByUser(user, pageRequest);
        boolean isLast = carts.isLast();
        int totalPage = carts.getTotalPages();
        long totalElement = carts.getTotalElements();

        List<CartResponseDTO> cartDTOs = carts.getContent().stream()
                .map(cart -> CartResponseDTO.of(cart, cart.getProduct(), productService)).toList();

        double totalPrice = cartDTOs.stream()
                .mapToDouble(cartDTO -> cartDTO.productPrice() * cartDTO.quantity())
                .sum();

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
                .orElseThrow(() -> CustomException.of(Error.NOT_FOUND_ERROR));

        List<Cart> existingCarts = cartRepository.findByUserAndProduct(cart.getUser(), cart.getProduct()).stream()
                .filter(c -> !c.getId().equals(cart.getId()) && c.getSelectOption().equals(request.selectOption())
                && c.getInputOption().equals(request.inputOption()))
                .toList();

        if (!existingCarts.isEmpty()) {
            Cart existingCart = existingCarts.get(0);
            existingCart.setQuantity(existingCart.getQuantity() + request.quantity());
            cartRepository.delete(cart);
            Cart updatedCart = cartRepository.save(existingCart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        } else {
            cart.setSelectOption(request.selectOption());
            cart.setInputOption(request.inputOption());
            cart.setQuantity(request.quantity());
            Cart updatedCart = cartRepository.save(cart);
            return CreatedCartDTO.builder().id(updatedCart.getId()).build();
        }
    }

}
