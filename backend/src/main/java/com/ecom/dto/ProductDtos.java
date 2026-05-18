package com.ecom.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            String imageUrl,
            Boolean active,
            @Min(0) Integer initialStockQuantity
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            String imageUrl,
            boolean active
    ) {
    }
}
