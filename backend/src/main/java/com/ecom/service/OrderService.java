package com.ecom.service;

import com.ecom.client.InventoryClient;
import com.ecom.client.InventoryClient.ReservedProduct;
import com.ecom.dto.OrderDtos.CreateOrderRequest;
import com.ecom.dto.OrderDtos.OrderItemResponse;
import com.ecom.dto.OrderDtos.OrderResponse;
import com.ecom.entity.Order;
import com.ecom.entity.OrderItem;
import com.ecom.entity.User;
import com.ecom.exception.ApiException;
import com.ecom.repository.OrderRepository;
import com.ecom.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Principal principal) {
        User user = currentUser(principal);
        Order order = new Order();
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;
        for (var line : request.items()) {
            ReservedProduct product = inventoryClient.reserveStock(
                    line.productId(),
                    line.quantity(),
                    "Order reservation",
                    principal
            );

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.productId());
            item.setProductName(product.productName());
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.unitPrice());
            order.getItems().add(item);

            total = total.add(product.unitPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }

        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Principal principal) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(principal.getName()).stream()
                .map(this::toResponse)
                .toList();
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}
