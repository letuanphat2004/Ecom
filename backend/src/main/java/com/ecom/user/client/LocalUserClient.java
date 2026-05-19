package com.ecom.user.client;

import com.ecom.exception.ApiException;
import com.ecom.user.repository.UserRepository;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LocalUserClient implements UserClient {

    private final UserRepository userRepository;

    public LocalUserClient(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserView getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }

        return userRepository.findByEmail(principal.getName())
                .map(user -> new UserView(user.getId(), user.getEmail(), user.getFullName()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
