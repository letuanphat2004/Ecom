package com.ecom.order.service;

import com.ecom.inventory.client.InventoryClient;
import com.ecom.inventory.client.InventoryClient.ReservedProduct;
import com.ecom.order.dto.OrderDtos.CreateOrderRequest;
import com.ecom.order.dto.OrderDtos.OrderItemResponse;
import com.ecom.order.dto.OrderDtos.OrderResponse;
import com.ecom.order.entity.Order;
import com.ecom.order.entity.OrderItem;
import com.ecom.order.repository.OrderRepository;
import com.ecom.user.client.UserClient;
import com.ecom.user.client.UserClient.UserView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, UserClient userClient, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Principal principal) {
        UserView user = userClient.getCurrentUser(principal);
        Order order = new Order();
        order.setUserId(user.userId());
        order.setCustomerEmail(user.email());
        order.setCustomerName(user.fullName());

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
        UserView user = userClient.getCurrentUser(principal);
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(user.email()).stream()
                .map(this::toResponse)
                .toList();
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
