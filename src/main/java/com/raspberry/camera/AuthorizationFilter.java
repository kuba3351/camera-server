package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.*;

class AuthorizationFilter implements Filter {
    private final UsernameAndPasswordDTO usernameAndPasswordDTO;

    private Map<String, Integer> numberOfTries;
    private Map<String, LocalDateTime> resetDate;

    private final static Logger logger = Logger.getLogger(AuthorizationFilter.class);

    public AuthorizationFilter(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
        this.numberOfTries = new HashMap<>();
        this.resetDate = new HashMap<>();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String ipAddress = servletRequest.getRemoteAddr();
        Integer tries = numberOfTries.get(ipAddress);
        try {
            if (tries != null && tries >= 5) {
                LocalDateTime date = resetDate.get(ipAddress);
                if (date != null && date.isBefore(LocalDateTime.now())) {
                    numberOfTries.put(ipAddress, 0);
                } else {
                    logger.info("Requesty z adresu ip:" + ipAddress + " zablokowane. Zbyt duża ilość prób.");
                    HttpServletResponse response = (HttpServletResponse) servletResponse;
                    response.setStatus(403);
                    response.getOutputStream().write("Request zablokowany z powodu zbyt dużej liczby prób".getBytes());
                    return;
                }
            }
            if(!((HttpServletRequest)servletRequest).getServletPath().startsWith("/api")) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            String authToken = httpRequest.getHeader("auth.token");
            if (authToken != null) {
                byte[] decodedToken = Base64.getDecoder().decode(authToken);
                String decodedString = new String(decodedToken);
                String[] splittedToken = decodedString.split(";");
                String username = splittedToken[0];
                LocalDateTime tokenDateTime = LocalDateTime.parse(splittedToken[1]);
                LocalDateTime expirationTime = LocalDateTime.parse(splittedToken[2]);
                String passwordCharsString = splittedToken[3];

                if (isTokenValid(username, tokenDateTime, expirationTime, passwordCharsString)) {
                    if (LocalDateTime.now().isBefore(expirationTime)) {
                        Authentication authentication = new AuthorityInfo(usernameAndPasswordDTO);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        filterChain.doFilter(servletRequest, servletResponse);
                    } else {
                        HttpServletResponse response = (HttpServletResponse) servletResponse;
                        response.setStatus(403);
                        response.getOutputStream().write("Token nieważny.".getBytes());
                    }
                }
                else throw new Exception();
            } else {
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.setStatus(403);
                response.getOutputStream().write("Brak tokena autoryzacyjnego".getBytes());
            }
        }catch (Exception e) {
            e.printStackTrace();
            LocalDateTime date = resetDate.get(ipAddress);
            if(date != null && date.isBefore(LocalDateTime.now())) {
                numberOfTries.put(ipAddress, 0);
            }
            else {
                numberOfTries.put(ipAddress, tries != null ? tries + 1 : 1);
                resetDate.put(ipAddress, LocalDateTime.now().plusMinutes(15));
            }
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            response.setStatus(403);
            response.getOutputStream().write("Nieprawidłowy token autoryzacyjny".getBytes());
        }
    }

    private boolean isTokenValid(String username, LocalDateTime tokenDateTime, LocalDateTime expirationTime, String passwordCharsString) {
        Duration duration = Duration.between(tokenDateTime, expirationTime);
        String password = usernameAndPasswordDTO.getPassword();
        int length = password.length();

        return tokenDateTime.equals(SecurityConfig.appStartDate)
                && username.equals(usernameAndPasswordDTO.getUsername())
                && passwordCharsString.charAt(0) == password.charAt(Long.valueOf(duration.get(SECONDS) % length).intValue())
                && passwordCharsString.charAt(1) == password.charAt(Long.valueOf(duration.get(NANOS) % length).intValue())
                && passwordCharsString.charAt(2) == password.charAt(Long.valueOf((duration.get(SECONDS)/10) % length).intValue())
                && passwordCharsString.charAt(3) == password.charAt((Math.abs(duration.hashCode() % length)));
    }

    @Override
    public void destroy() {

    }
}
