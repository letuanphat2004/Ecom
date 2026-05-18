package com.ecom.inventory.service;

import com.ecom.exception.ApiException;
import com.ecom.inventory.dto.InventoryDtos.AdjustStockRequest;
import com.ecom.inventory.dto.InventoryDtos.InventoryMovementResponse;
import com.ecom.inventory.dto.InventoryDtos.RestockRequest;
import com.ecom.inventory.dto.InventoryDtos.StockResponse;
import com.ecom.inventory.entity.InventoryMovement;
import com.ecom.inventory.entity.InventoryMovementType;
import com.ecom.inventory.entity.StockItem;
import com.ecom.inventory.repository.InventoryMovementRepository;
import com.ecom.inventory.repository.StockItemRepository;
import com.ecom.product.client.ProductClient;
import com.ecom.product.client.ProductClient.ProductView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ProductClient productClient;
    private final StockItemRepository stockItemRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(
            ProductClient productClient,
            StockItemRepository stockItemRepository,
            InventoryMovementRepository movementRepository
    ) {
        this.productClient = productClient;
        this.stockItemRepository = stockItemRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAllStock() {
        List<ProductView> products = productClient.findAllProducts();
        if (products.isEmpty()) {
            return List.of();
        }

        Map<Long, StockItem> stockByProductId = stockItemRepository.findByProductIdIn(
                        products.stream().map(ProductView::productId).toList()
                ).stream()
                .collect(Collectors.toMap(StockItem::getProductId, stockItem -> stockItem));

        return products.stream()
                .map(product -> toStockResponse(product, stockByProductId.get(product.productId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public StockResponse findProductStock(Long productId) {
        ProductView product = productClient.getProduct(productId);
        StockItem stockItem = stockItemRepository.findByProductId(productId).orElse(null);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public StockResponse restock(Long productId, RestockRequest request, Principal principal) {
        ProductView product = productClient.getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        int nextStock = stockItem.getQuantity() + request.quantity();
        stockItem.setQuantity(nextStock);
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.RESTOCK, request.quantity(), nextStock, request.reason(), principal);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public StockResponse adjustStock(Long productId, AdjustStockRequest request, Principal principal) {
        ProductView product = productClient.getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        int quantityChange = request.stockQuantity() - stockItem.getQuantity();
        stockItem.setQuantity(request.stockQuantity());
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.ADJUSTMENT, quantityChange, stockItem.getQuantity(), request.reason(), principal);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public StockResponse initializeStock(Long productId, int initialQuantity, String reason, Principal principal) {
        if (initialQuantity < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Initial stock quantity must be greater than or equal to 0");
        }

        ProductView product = productClient.getProduct(productId);
        if (stockItemRepository.findByProductIdForUpdate(productId).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock already initialized for product: " + product.productName());
        }

        StockItem stockItem = new StockItem();
        stockItem.setProductId(productId);
        stockItem.setQuantity(initialQuantity);
        stockItemRepository.save(stockItem);
        saveMovement(product, InventoryMovementType.INITIALIZATION, initialQuantity, initialQuantity, reason, principal);
        return toStockResponse(product, stockItem);
    }

    @Transactional
    public ProductView reserveStock(Long productId, int quantity, String reason, Principal principal) {
        ProductView product = productClient.getProduct(productId);
        StockItem stockItem = getOrCreateLockedStockItem(productId);
        if (!product.active()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not active: " + product.productId());
        }
        if (stockItem.getQuantity() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.productName());
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
            ProductView product,
            InventoryMovementType type,
            int quantityChange,
            int stockAfter,
            String reason,
            Principal principal
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(product.productId());
        movement.setProductName(product.productName());
        movement.setType(type);
        movement.setQuantityChange(quantityChange);
        movement.setStockAfter(stockAfter);
        movement.setReason(reason);
        movement.setCreatedBy(principal != null ? principal.getName() : "system");
        movementRepository.save(movement);
    }

    private StockResponse toStockResponse(ProductView product, StockItem stockItem) {
        return new StockResponse(
                product.productId(),
                product.productName(),
                stockItem != null ? stockItem.getQuantity() : 0,
                product.active()
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
