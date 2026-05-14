package com.ecom.controller;

import com.ecom.dto.InventoryDtos.AdjustStockRequest;
import com.ecom.dto.InventoryDtos.InventoryMovementResponse;
import com.ecom.dto.InventoryDtos.RestockRequest;
import com.ecom.dto.InventoryDtos.StockResponse;
import com.ecom.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<StockResponse> findAllStock() {
        return inventoryService.findAllStock();
    }

    @GetMapping("/{productId}")
    public StockResponse findProductStock(@PathVariable Long productId) {
        return inventoryService.findProductStock(productId);
    }

    @PatchMapping("/{productId}/restock")
    public StockResponse restock(
            @PathVariable Long productId,
            @Valid @RequestBody RestockRequest request,
            Principal principal
    ) {
        return inventoryService.restock(productId, request, principal);
    }

    @PatchMapping("/{productId}/adjust")
    public StockResponse adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody AdjustStockRequest request,
            Principal principal
    ) {
        return inventoryService.adjustStock(productId, request, principal);
    }

    @GetMapping("/movements")
    public List<InventoryMovementResponse> findRecentMovements(@RequestParam(required = false) Long productId) {
        return inventoryService.findRecentMovements(productId);
    }
}
