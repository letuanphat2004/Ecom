package com.ecom.client;

import java.math.BigDecimal;
import java.security.Principal;

public interface InventoryClient {

    ReservedProduct reserveStock(Long productId, int quantity, String reason, Principal principal);

    record ReservedProduct(
            Long productId,
            String productName,
            BigDecimal unitPrice
    ) {
    }
}
