package com.ecom.user.client;

import java.util.Optional;
import java.util.Set;

public interface UserAccountClient {

    boolean existsByEmail(String email);

    UserAccountView createCustomer(String fullName, String email, String encodedPassword);

    Optional<UserAccountView> findByEmail(String email);

    record UserAccountView(
            Long userId,
            String fullName,
            String email,
            Set<String> roles
    ) {
    }
}
