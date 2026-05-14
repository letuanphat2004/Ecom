package com.ecom.config;

import com.ecom.entity.Product;
import com.ecom.entity.Role;
import com.ecom.entity.User;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    public DataSeeder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner seedInitialData(UserRepository userRepository, ProductRepository productRepository) {
        return args -> {
            ensureUser(userRepository, "Ecom Admin", "admin@ecom.local", "admin123", Role.ROLE_ADMIN);
            ensureUser(userRepository, "Customer User", "user@ecom.local", "user123", Role.ROLE_CUSTOMER);

            if (productRepository.count() == 0) {
                productRepository.save(product(
                        "Ao thun cotton",
                        "Ao thun co ban, chat lieu cotton thoang mat.",
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80",
                        159000,
                        42
                ));
                productRepository.save(product(
                        "Giay sneaker trang",
                        "Giay di hang ngay voi thiet ke toi gian.",
                        "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=900&q=80",
                        620000,
                        18
                ));
                productRepository.save(product(
                        "Balo laptop",
                        "Balo ngan laptop 15 inch, phu hop di hoc va di lam.",
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=80",
                        390000,
                        25
                ));
            }
        };
    }

    private Product product(String name, String description, String imageUrl, int price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stock);
        product.setActive(true);
        return product;
    }

    private void ensureUser(
            UserRepository userRepository,
            String fullName,
            String email,
            String rawPassword,
            Role role
    ) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
