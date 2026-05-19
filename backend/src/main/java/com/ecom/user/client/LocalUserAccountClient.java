package com.ecom.user.client;

import com.ecom.user.entity.Role;
import com.ecom.user.entity.User;
import com.ecom.user.repository.UserRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LocalUserAccountClient implements UserAccountClient {

    private final UserRepository userRepository;

    public LocalUserAccountClient(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserAccountView createCustomer(String fullName, String email, String encodedPassword) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.getRoles().add(Role.ROLE_CUSTOMER);
        return toView(userRepository.save(user));
    }

    @Override
    public Optional<UserAccountView> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toView);
    }

    private UserAccountView toView(User user) {
        return new UserAccountView(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
        );
    }
}
