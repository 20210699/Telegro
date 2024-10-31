package com.telegro.telegro.domain.order.dto.response;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import com.telegro.telegro.domain.cart.dto.response.CartResponseDTO;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentMethod;
import com.telegro.telegro.domain.order.entity.enums.PaymentStatus;
import com.telegro.telegro.domain.product.dto.response.ProductDetailResponseDTO;
import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDetailDTO(
        @Schema(description = "주문 id")
        Long id,
        @Schema(description = "주문 번호")
        String orderNumber,
        @Schema(description = "상품 List")
        List<CartProductDTO> products,
        @Schema(description = "주문 날짜")
        LocalDateTime createdAt,
        @Schema(description = "주문 상태")
        OrderStatus orderStatus,
        @Schema(description = "배송비")
        BigDecimal shoppingCost,
        @Schema(description = "결제 방식")
        PaymentMethod paymentMethod,
        @Schema(description = "결제 상태")
        PaymentStatus paymentStatus,
        @Schema(description = "배송비 정보")
        DeliveryAddress deliveryAddress
) {
        public static OrderDetailDTO of(Order order, List<CartProductDTO> products) {
                return OrderDetailDTO.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .products(products)
                        .createdAt(order.getCreatedAt())
                        .orderStatus(order.getOrderStatus())
                        .shoppingCost(order.getShippingCost())
                        .paymentMethod(order.getPaymentMethod())
                        .paymentStatus(order.getPaymentStatus())
                        .deliveryAddress(order.getDeliveryAddress())
                        .build();
        }
}
