package com.raspberry.camera.config;

import com.raspberry.camera.dto.UsernameAndPasswordDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * Klasa przepuszczająca wszystkie żądania do API kiedy NIE jest ono zabezpieczone
 */
public class PermitAllFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
        usernameAndPasswordDTO.setUsername("default");
        usernameAndPasswordDTO.setPassword("default");
        Authentication authentication = new AuthorityInfo(usernameAndPasswordDTO);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
