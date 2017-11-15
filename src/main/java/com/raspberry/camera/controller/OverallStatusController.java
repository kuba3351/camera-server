package com.raspberry.camera.controller;

import com.raspberry.camera.service.ConfigFileService;
import com.raspberry.camera.service.DatabaseService;
import com.raspberry.camera.service.NetworkService;
import com.raspberry.camera.dto.OveralStateDTO;
import com.raspberry.camera.service.PendriveService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class OverallStatusController {

    private ConfigFileService configFileService;

    private DatabaseService databaseService;

    private NetworkService networkService;

    private PendriveService pendriveService;

    private final static Logger logger = Logger.getLogger(OverallStatusController.class);

    @Autowired
    public OverallStatusController(ConfigFileService configFileService, DatabaseService databaseService, NetworkService networkService, PendriveService pendriveService) {
        this.configFileService = configFileService;
        this.databaseService = databaseService;
        this.networkService = networkService;
        this.pendriveService = pendriveService;
    }

    @GetMapping("getAppStatus")
    public OveralStateDTO getAppStatus() throws IOException, InterruptedException {
        logger.info("Żądanie pobrania informacji o stanie serwera...");
        OveralStateDTO overalStateDTO = new OveralStateDTO();
        overalStateDTO.setDatabaseConnected(databaseService.getDatabaseSession() != null);
        overalStateDTO.setDatabaseEnabled(configFileService.getSavingPlacesDTO().getJpgDatabaseSave() ||
                configFileService.getSavingPlacesDTO().getMatDatabaseSave());
        overalStateDTO.setHotspotEnabled(networkService.getHotspotActive());
        overalStateDTO.setSecurityEnabled(configFileService.getUsernameAndPasswordDTO().getEnabled());
        overalStateDTO.setJpgComputerSaveEnabled(configFileService.getSavingPlacesDTO().getJpgComputerSave());
        overalStateDTO.setJpgLocation(configFileService.getSavingPlacesDTO().getJpgComputerLocation());
        int cameras = 0;
        logger.info("Rozpoczynam wykrywanie podłączonych kamer...");
        if(new File("/dev/video0").exists())
            cameras++;
        if(new File("/dev/video1").exists())
            cameras++;
        logger.info("Liczba wykrytych kamer:"+cameras);
        overalStateDTO.setCameras(cameras);
        overalStateDTO.setPendriveEnabled(configFileService.getSavingPlacesDTO().getMatPendriveSave() ||
                configFileService.getSavingPlacesDTO().getJpgRaspberryPendriveSave());
        overalStateDTO.setPendriveConnected(pendriveService.checkIfPendriveConnected());
        overalStateDTO.setPendriveMounted(pendriveService.checkWherePendriveMounted().isPresent());
        return overalStateDTO;
    }
}
