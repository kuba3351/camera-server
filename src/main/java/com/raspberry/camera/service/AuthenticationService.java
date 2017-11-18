package com.raspberry.camera.service;

import com.raspberry.camera.dto.UsernameAndPasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuthenticationService {

    private UsernameAndPasswordDTO usernameAndPasswordDTO;

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) throws IOException {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
        configFileService.writeAuthInfo(usernameAndPasswordDTO);
    }

    private ConfigFileService configFileService;

    @Autowired
    public AuthenticationService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.usernameAndPasswordDTO = configFileService.getUsernameAndPasswordDTO();
    }

}
