package com.raspberry.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OverallStatusController {

    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private NetworkService networkService;

    @GetMapping("getAppStatus")
    public OveralStateDTO getAppStatus() {
        OveralStateDTO overalStateDTO = new OveralStateDTO();
        overalStateDTO.setDatabaseConnected(databaseService.getDatabaseSession() != null);
        overalStateDTO.setDatabaseEnabled(configFileService.getSavingPlacesDTO().getJpgDatabaseSave() || configFileService.getSavingPlacesDTO().getMatDatabaseSave());
        overalStateDTO.setHotspotEnabled(networkService.getHotspotActive());
        overalStateDTO.setSecurityEnabled(configFileService.getUsernameAndPasswordDTO().getEnabled());
        return overalStateDTO;
    }
}
