package com.raspberry.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.*;

import static java.time.temporal.ChronoUnit.*;

@RestController
public class AuthenticationController {

    @Autowired
    ConfigFileService configFileService;

    @PostMapping("/getAuthToken")
    public ResponseEntity getToken(@RequestBody UsernameAndPasswordDTO usernameAndPasswordDTO) {
        if(usernameAndPasswordDTO.getUsername().equals(configFileService.getUsernameAndPasswordDTO().getUsername())
                && usernameAndPasswordDTO.getPassword().equals(configFileService.getUsernameAndPasswordDTO().getPassword())) {
            StringBuilder token = new StringBuilder();
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(3);
            token.append(usernameAndPasswordDTO.getUsername());
            token.append(";");
            token.append(SecurityConfig.appStartDate);
            token.append(";");
            StringBuilder passwordCharsString = new StringBuilder();
            Duration duration = Duration.between(SecurityConfig.appStartDate, expiration);
            String password = usernameAndPasswordDTO.getPassword();
            int passwordLength = password.length();
            passwordCharsString.append(password.charAt(Long.valueOf(duration.get(SECONDS) % passwordLength).intValue()));
            passwordCharsString.append(password.charAt(Long.valueOf(duration.get(NANOS) % passwordLength).intValue()));
            passwordCharsString.append(password.charAt(Long.valueOf((duration.get(SECONDS)/10) % passwordLength).intValue()));
            passwordCharsString.append(password.charAt(Math.abs(duration.hashCode() % passwordLength)));
            token.append(expiration);
            token.append(";");
            token.append(passwordCharsString);
            UsernameAndPasswordDTO response = new UsernameAndPasswordDTO();
            response.setToken(new String(Base64.encode(token.toString().getBytes())));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>("Bad username or password", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/api/saveAuthInfo")
    public ResponseEntity saveAuthInfo(@RequestBody UsernameAndPasswordDTO usernameAndPasswordDTO) throws IOException {
        configFileService.writeAuthInfo(usernameAndPasswordDTO);
        return new ResponseEntity(HttpStatus.OK);
    }
}
