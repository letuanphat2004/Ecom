package com.ecom.client;

import com.ecom.entity.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.exception.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LocalProductClient implements ProductClient {

    private final ProductRepository productRepository;

    public LocalProductClient(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductView> findAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductView)
                .toList();
    }

    @Override
    public ProductView getProduct(Long productId) {
        return productRepository.findById(productId)
                .map(this::toProductView)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductView toProductView(Product product) {
        return new ProductView(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.isActive()
        );
    }
}
