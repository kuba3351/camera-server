package com.raspberry.camera;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;

import static java.time.temporal.ChronoUnit.*;

class AuthorizationFilter implements Filter {
    private final UsernameAndPasswordDTO usernameAndPasswordDTO;

    public AuthorizationFilter(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String authToken = httpRequest.getHeader("auth.token");
        if(authToken != null) {
            byte[] decodedToken = Base64.getDecoder().decode(authToken);
            String decodedString = new String(decodedToken);
            String[] splittedToken = decodedString.split(";");
            String username = splittedToken[0];
            LocalDateTime tokenDateTime = LocalDateTime.parse(splittedToken[1]);
            LocalDateTime expirationTime = LocalDateTime.parse(splittedToken[2]);
            String passwordCharsString = splittedToken[3];

            if (isTokenValid(username, tokenDateTime, expirationTime, passwordCharsString)) {
                Authentication authentication = new AuthorityInfo(usernameAndPasswordDTO);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isTokenValid(String username, LocalDateTime tokenDateTime, LocalDateTime expirationTime, String passwordCharsString) {
        Duration duration = Duration.between(tokenDateTime, expirationTime);
        String password = usernameAndPasswordDTO.getPassword();
        int length = password.length();

        return tokenDateTime.equals(SecurityConfig.appStartDate)
                && username.equals(usernameAndPasswordDTO.getUsername())
                && LocalDateTime.now().isBefore(expirationTime)
                && passwordCharsString.charAt(0) == password.charAt(Long.valueOf(duration.get(SECONDS) % length).intValue())
                && passwordCharsString.charAt(1) == password.charAt(Long.valueOf(duration.get(NANOS) % length).intValue())
                && passwordCharsString.charAt(2) == password.charAt(Long.valueOf((duration.get(SECONDS)/10) % length).intValue())
                && passwordCharsString.charAt(3) == password.charAt((Math.abs(duration.hashCode() % length)));
    }

    @Override
    public void destroy() {

    }
}
