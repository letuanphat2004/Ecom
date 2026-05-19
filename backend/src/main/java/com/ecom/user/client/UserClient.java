package com.ecom.user.client;

import java.security.Principal;

public interface UserClient {

    UserView getCurrentUser(Principal principal);

    record UserView(
            Long userId,
            String email,
            String fullName
    ) {
    }
}
