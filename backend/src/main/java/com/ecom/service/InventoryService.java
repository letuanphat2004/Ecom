package com.ecom.service;

import com.ecom.dto.InventoryDtos.AdjustStockRequest;
import com.ecom.dto.InventoryDtos.InventoryMovementResponse;
import com.ecom.dto.InventoryDtos.RestockRequest;
import com.ecom.dto.InventoryDtos.StockResponse;
import com.ecom.entity.InventoryMovement;
import com.ecom.entity.InventoryMovementType;
import com.ecom.entity.Product;
import com.ecom.entity.StockItem;
import com.ecom.exception.ApiException;
import com.ecom.repository.InventoryMovementRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.StockItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(
            ProductRepository productRepository,
            StockItemRepository stockItemRepository,
            InventoryMovementRepository movementRepository
    ) {
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAllStock() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return List.of();
        }

        Map<Long, StockItem> stockByProductId = stockItemRepository.findByProductIdIn(
                        products.stream().map(Product::getId).toList()
                ).stream()
                .collect(Collectors.toMap(StockItem::getProductId, stockItem -> stockItem));

        return products.stream()
                .map(product -> toStockResponse(product, stockByProductId.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse findProductStock(Long productId) {
        Product product = getProduct(productId);
        StockItem stockItem = stockItemRepository.findByProductId(productId).orElse(null);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public StockResponse restock(Long productId, RestockRequest request, Principal principal) {
        Product product = getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        int nextStock = stockItem.getQuantity() + request.quantity();
        stockItem.setQuantity(nextStock);
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.RESTOCK, request.quantity(), nextStock, request.reason(), principal);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public StockResponse adjustStock(Long productId, AdjustStockRequest request, Principal principal) {
        Product product = getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        int quantityChange = request.stockQuantity() - stockItem.getQuantity();
        stockItem.setQuantity(request.stockQuantity());
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.ADJUSTMENT, quantityChange, stockItem.getQuantity(), request.reason(), principal);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public Product reserveStock(Long productId, int quantity, String reason, Principal principal) {
        Product product = getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        if (!product.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not active: " + product.getId());
        }
        if (stockItem.getQuantity() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
        }

        int nextStock = stockItem.getQuantity() - quantity;
        stockItem.setQuantity(nextStock);
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.RESERVATION, -quantity, nextStock, reason, principal);
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

    private StockItem getOrCreateLockedStockItem(Long productId) {
        return stockItemRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> {
                    StockItem stockItem = new StockItem();
                    stockItem.setProductId(productId);
                    stockItem.setQuantity(0);
                    return stockItemRepository.saveAndFlush(stockItem);
                });
    }

    private void saveMovement(
            Product product,
            InventoryMovementType type,
            int quantityChange,
            int stockAfter,
            String reason,
            Principal principal
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(product.getId());
        movement.setProductName(product.getName());
        movement.setType(type);
        movement.setQuantityChange(quantityChange);
        movement.setStockAfter(stockAfter);
        movement.setReason(reason);
        movement.setCreatedBy(principal != null ? principal.getName() : "system");
        movementRepository.save(movement);
    }

    private StockResponse toStockResponse(Product product, StockItem stockItem) {
        return new StockResponse(
                product.getId(),
                product.getName(),
                stockItem != null ? stockItem.getQuantity() : 0,
                product.isActive()
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getProductName(),
                movement.getType(),
                movement.getQuantityChange(),
                movement.getStockAfter(),
                movement.getReason(),
                movement.getCreatedBy(),
                movement.getCreatedAt()
        );
    }
}
