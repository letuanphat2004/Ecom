package com.ecom.service;

import com.ecom.dto.InventoryDtos.AdjustStockRequest;
import com.ecom.dto.InventoryDtos.InventoryMovementResponse;
import com.ecom.dto.InventoryDtos.RestockRequest;
import com.ecom.dto.InventoryDtos.StockResponse;
import com.ecom.entity.InventoryMovement;
import com.ecom.entity.InventoryMovementType;
import com.ecom.entity.Product;
import com.ecom.exception.ApiException;
import com.ecom.repository.InventoryMovementRepository;
import com.ecom.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(ProductRepository productRepository, InventoryMovementRepository movementRepository) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAllStock() {
        return productRepository.findAll().stream()
                .map(this::toStockResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse findProductStock(Long productId) {
        return toStockResponse(getProduct(productId));
    }

    @Transactional
    public StockResponse restock(Long productId, RestockRequest request, Principal principal) {
        Product product = getLockedProduct(productId);
        int nextStock = product.getStockQuantity() + request.quantity();
        product.setStockQuantity(nextStock);
        saveMovement(product, InventoryMovementType.RESTOCK, request.quantity(), request.reason(), principal);
        return toStockResponse(product);
    }

    @Transactional
    public StockResponse adjustStock(Long productId, AdjustStockRequest request, Principal principal) {
        Product product = getLockedProduct(productId);
        int quantityChange = request.stockQuantity() - product.getStockQuantity();
        product.setStockQuantity(request.stockQuantity());
        saveMovement(product, InventoryMovementType.ADJUSTMENT, quantityChange, request.reason(), principal);
        return toStockResponse(product);
    }

    @Transactional
    public Product reserveStock(Long productId, int quantity, String reason, Principal principal) {
        Product product = getLockedProduct(productId);
        if (!product.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not active: " + product.getId());
        }
        if (product.getStockQuantity() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        saveMovement(product, InventoryMovementType.RESERVATION, -quantity, reason, principal);
        return product;
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findRecentMovements(Long productId) {
        List<InventoryMovement> movements = productId == null
                ? movementRepository.findTop50ByOrderByCreatedAtDesc()
                : movementRepository.findTop50ByProductIdOrderByCreatedAtDesc(productId);

        return movements.stream()
                .map(this::toMovementResponse)
                .toList();
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Product getLockedProduct(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void saveMovement(
            Product product,
            InventoryMovementType type,
            int quantityChange,
            String reason,
            Principal principal
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantityChange(quantityChange);
        movement.setStockAfter(product.getStockQuantity());
        movement.setReason(reason);
        movement.setCreatedBy(principal != null ? principal.getName() : "system");
        movementRepository.save(movement);
    }

    private StockResponse toStockResponse(Product product) {
        return new StockResponse(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                product.isActive()
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getProduct().getName(),
                movement.getType(),
                movement.getQuantityChange(),
                movement.getStockAfter(),
                movement.getReason(),
                movement.getCreatedBy(),
                movement.getCreatedAt()
        );
    }
}
