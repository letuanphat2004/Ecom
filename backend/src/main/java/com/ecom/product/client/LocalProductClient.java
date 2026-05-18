package com.ecom.product.client;

import com.ecom.exception.ApiException;
import com.ecom.product.entity.Product;
import com.ecom.product.repository.ProductRepository;
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
