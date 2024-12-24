package com.telegro.telegro.domain.order.dto.response;

import com.telegro.telegro.domain.cart.dto.response.CartProductDTO;
import com.telegro.telegro.domain.order.entity.Order;
import com.telegro.telegro.domain.order.entity.enums.OrderStatus;
import com.telegro.telegro.domain.order.entity.enums.PaymentMethod;
import com.telegro.telegro.domain.user.dto.response.DeliveryAddressDetailDTO;
import com.telegro.telegro.domain.user.dto.response.UserOrderDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDetailResponseDTO(
        @Schema(description = "주문 ID")
        Long orderId,
        @Schema(description = "주문 일자")
        LocalDateTime orderDate,
        @Schema(description = "주문 번호")
        String imp_uid,
        @Schema(description = "주문 상태")
        OrderStatus orderStatus,
        @Schema(description = "주문 요청 사항")
        String request,
        @Schema(description = "주문 상품 List")
        List<CartProductDTO> products,
        @Schema(description = "구매자 정보")
        UserOrderDetailDTO user,
        @Schema(description = "배송지 정보")
        DeliveryAddressDetailDTO deliveryAddress,
        @Schema(description = "주문 금액")
        BigDecimal price,
        @Schema(description = "할인 금액")
        BigDecimal discountPrice,
        @Schema(description = "배송비")
        BigDecimal shippingCost,
        @Schema(description = "실제로 결제한 금액")
        BigDecimal totalPrice,
        @Schema(description = "결제 수단")
        PaymentMethod paymentMethod,
        @Schema(description = "매출전표 URL")
        String receipt_url,
        @Schema(description = "현금 영수증 URL")
        String cash_receipt_url
) {
        public static OrderDetailResponseDTO of(Order order) {
                List<CartProductDTO> products = order.getCarts().stream().map(CartProductDTO::of).toList();

                UserOrderDetailDTO user = UserOrderDetailDTO.of(order.getUser());

                DeliveryAddressDetailDTO deliveryAddress = DeliveryAddressDetailDTO.of(order.getDeliveryAddress(), false);

                BigDecimal price = products.stream()
                        .map(CartProductDTO::totalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal discountPrice = price.subtract(order.getTotalPrice())
                                                .add(order.getShippingCost());

                return OrderDetailResponseDTO.builder()
                        .orderId(order.getId())
                        .orderDate(order.getCreatedAt())
                        .imp_uid(order.getOrderNumber())
                        .orderStatus(order.getOrderStatus())
                        .request(order.getRequest())
                        .products(products)
                        .user(user)
                        .deliveryAddress(deliveryAddress)
                        .price(price)
                        .discountPrice(discountPrice)
                        .shippingCost(order.getShippingCost())
                        .totalPrice(order.getTotalPrice())
                        .paymentMethod(order.getPaymentMethod())
                        .receipt_url(order.getReceiptUrl())
                        .cash_receipt_url(order.getCashReceiptUrl())
                        .build();
        }
}