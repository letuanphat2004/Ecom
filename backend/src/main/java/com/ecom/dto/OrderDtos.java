package com.ecom.dto;

import com.ecom.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotEmpty List<@Valid OrderLineRequest> items
    ) {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {
    }

    public record OrderResponse(
            Long id,
            OrderStatus status,
            BigDecimal totalAmount,
            Instant createdAt,
            List<OrderItemResponse> items
    ) {
    }

    public record OrderItemResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
    }
}
