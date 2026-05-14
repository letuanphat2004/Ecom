package com.ecom.controller;

import com.ecom.dto.OrderDtos.CreateOrderRequest;
import com.ecom.dto.OrderDtos.OrderResponse;
import com.ecom.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
        return orderService.createOrder(request, principal);
    }

    @GetMapping("/me")
    public List<OrderResponse> myOrders(Principal principal) {
        return orderService.myOrders(principal);
    }
}
