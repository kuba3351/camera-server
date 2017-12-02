package com.raspberry.camera.controller;

import com.raspberry.camera.config.SecurityConfig;
import com.raspberry.camera.dto.UsernameAndPasswordDTO;
import com.raspberry.camera.service.AuthenticationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Kontroler odpowiedzialny za obsługę zabezpieczenia API
 */
@RestController
public class AuthenticationController {

    private final static Logger logger = Logger.getLogger(AuthenticationController.class);
    private AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Pobieranie tokena do uwierzytelnienia
     *
     * @param usernameAndPasswordDTO Nazwa użytkownika i hasło
     * @return
     */
    @PostMapping("/getAuthToken")
    public ResponseEntity getToken(@RequestBody UsernameAndPasswordDTO usernameAndPasswordDTO) {
        if (areCredentialsCorrect(usernameAndPasswordDTO)) {
            String token = generateToken(usernameAndPasswordDTO);
            UsernameAndPasswordDTO response = new UsernameAndPasswordDTO();
            response.setToken(new String(Base64.encode(token.getBytes())));
            logger.info("Wygenerowano nowy token dla:" + usernameAndPasswordDTO.getUsername());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        logger.error("Błędne dane uwierzytelniające. Nie można utworzyć tokena.");
        return new ResponseEntity<>("Bad username or password", HttpStatus.FORBIDDEN);
    }

    private String generateToken(@RequestBody UsernameAndPasswordDTO usernameAndPasswordDTO) {
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
        passwordCharsString.append(password.charAt(Long.valueOf((duration.get(SECONDS) / 10) % passwordLength).intValue()));
        passwordCharsString.append(password.charAt(Math.abs(duration.hashCode() % passwordLength)));
        token.append(expiration);
        token.append(";");
        token.append(passwordCharsString);
        return token.toString();
    }

    private boolean areCredentialsCorrect(@RequestBody UsernameAndPasswordDTO usernameAndPasswordDTO) {
        UsernameAndPasswordDTO usernameAndPasswordDTO1 = authenticationService.getUsernameAndPasswordDTO();
        return usernameAndPasswordDTO.getUsername().equals(usernameAndPasswordDTO1.getUsername())
                && usernameAndPasswordDTO.getPassword().equals(usernameAndPasswordDTO1.getPassword());
    }

    /**
     * Aktualizacja danych uwierzytelniających
     *
     * @param usernameAndPasswordDTO nowa nazwa użytkownika i hasło
     * @return
     * @throws IOException
     */
    @PostMapping("/api/saveAuthInfo")
    public ResponseEntity saveAuthInfo(@RequestBody @Valid UsernameAndPasswordDTO usernameAndPasswordDTO) throws IOException {
        String password = usernameAndPasswordDTO.getPassword();
        if (password == null || password.isEmpty()) {
            usernameAndPasswordDTO
                    .setPassword(authenticationService.getUsernameAndPasswordDTO()
                            .getPassword());
        }
        authenticationService.setUsernameAndPasswordDTO(usernameAndPasswordDTO);
        logger.info("Zaktualizowano dane uwierzytelniające.");
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Pobieranie informacji o zabezpieczeniach API
     *
     * @return
     */
    @GetMapping("/api/getAuthInfo")
    public UsernameAndPasswordDTO getAuthInfo() {
        logger.info("Żądanie pobrania ustawień autentykacji...");
        UsernameAndPasswordDTO dtoFromConfig = authenticationService.getUsernameAndPasswordDTO();
        UsernameAndPasswordDTO responseDto = new UsernameAndPasswordDTO();
        responseDto.setEnabled(dtoFromConfig.getEnabled());
        responseDto.setUsername(dtoFromConfig.getUsername());
        return responseDto;
    }
}
