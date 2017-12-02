package com.raspberry.camera.service;

import com.raspberry.camera.dto.UsernameAndPasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Serwis służący do zarządzania zabezieczeniem API
 */
@Service
public class AuthenticationService {

    private UsernameAndPasswordDTO usernameAndPasswordDTO;
    private ConfigFileService configFileService;

    @Autowired
    public AuthenticationService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.usernameAndPasswordDTO = configFileService.getUsernameAndPasswordDTO();
    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) throws IOException {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
        configFileService.writeAuthInfo(usernameAndPasswordDTO);
    }

}
