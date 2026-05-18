package com.ecom.inventory.dto;

import com.ecom.inventory.entity.InventoryMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record StockResponse(
            Long productId,
            String productName,
            Integer stockQuantity,
            boolean active
    ) {
    }

    public record RestockRequest(
            @NotNull @Min(1) Integer quantity,
            String reason
    ) {
    }

    public record AdjustStockRequest(
            @NotNull @Min(0) Integer stockQuantity,
            String reason
    ) {
    }

    public record InventoryMovementResponse(
            Long id,
            Long productId,
            String productName,
            InventoryMovementType type,
            Integer quantityChange,
            Integer stockAfter,
            String reason,
            String createdBy,
            Instant createdAt
    ) {
    }
}
