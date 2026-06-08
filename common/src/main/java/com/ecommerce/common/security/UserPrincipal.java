package com.ecommerce.common.security;

import java.security.Principal;

public record UserPrincipal(Long id, String email) implements Principal {
    @Override
    public String getName() {
        return email;
    }
}