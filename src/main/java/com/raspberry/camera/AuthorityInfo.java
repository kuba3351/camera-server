package com.raspberry.camera;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

class AuthorityInfo implements Authentication {
    private final UsernameAndPasswordDTO usernameAndPasswordDTO;

    public AuthorityInfo(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList((GrantedAuthority) () -> "ADMIN");
    }

    @Override
    public Object getCredentials() {
        return usernameAndPasswordDTO.getPassword();
    }

    @Override
    public Object getDetails() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return usernameAndPasswordDTO.getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return usernameAndPasswordDTO.getUsername();
    }
}
