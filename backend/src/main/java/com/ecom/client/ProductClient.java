package com.ecom.client;

import java.math.BigDecimal;
import java.util.List;

public interface ProductClient {

    List<ProductView> findAllProducts();

    ProductView getProduct(Long productId);

    record ProductView(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            boolean active
    ) {
    }
}
