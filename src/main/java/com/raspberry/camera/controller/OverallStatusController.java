package com.raspberry.camera.controller;

import com.raspberry.camera.service.ConfigFileService;
import com.raspberry.camera.service.DatabaseService;
import com.raspberry.camera.service.NetworkService;
import com.raspberry.camera.dto.OveralStateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OverallStatusController {

    private ConfigFileService configFileService;

    private DatabaseService databaseService;

    private NetworkService networkService;

    @Autowired
    public OverallStatusController(ConfigFileService configFileService, DatabaseService databaseService, NetworkService networkService) {
        this.configFileService = configFileService;
        this.databaseService = databaseService;
        this.networkService = networkService;
    }

    @GetMapping("getAppStatus")
    public OveralStateDTO getAppStatus() {
        OveralStateDTO overalStateDTO = new OveralStateDTO();
        overalStateDTO.setDatabaseConnected(databaseService.getDatabaseSession() != null);
        overalStateDTO.setDatabaseEnabled(configFileService.getSavingPlacesDTO().getJpgDatabaseSave() || configFileService.getSavingPlacesDTO().getMatDatabaseSave());
        overalStateDTO.setHotspotEnabled(networkService.getHotspotActive());
        overalStateDTO.setSecurityEnabled(configFileService.getUsernameAndPasswordDTO().getEnabled());
        overalStateDTO.setJpgComputerSaveEnabled(configFileService.getSavingPlacesDTO().getJpgComputerSave());
        overalStateDTO.setJpgLocation(configFileService.getSavingPlacesDTO().getJpgComputerLocation());
        return overalStateDTO;
    }
}
