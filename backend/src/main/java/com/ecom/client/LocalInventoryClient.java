package com.ecom.client;

import com.ecom.product.client.ProductClient.ProductView;
import com.ecom.service.InventoryService;
import java.security.Principal;
import org.springframework.stereotype.Component;

@Component
public class LocalInventoryClient implements InventoryClient {

    private final InventoryService inventoryService;

    public LocalInventoryClient(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void initializeStock(Long productId, int initialQuantity, String reason, Principal principal) {
        inventoryService.initializeStock(productId, initialQuantity, reason, principal);
    }

    @Override
    public ReservedProduct reserveStock(Long productId, int quantity, String reason, Principal principal) {
        ProductView product = inventoryService.reserveStock(productId, quantity, reason, principal);
        return new ReservedProduct(product.productId(), product.productName(), product.unitPrice());
    }
}
