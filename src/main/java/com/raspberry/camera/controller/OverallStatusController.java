package com.raspberry.camera.controller;

import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.service.*;
import com.raspberry.camera.dto.OveralStateDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class OverallStatusController {

    private DatabaseService databaseService;
    private NetworkService networkService;
    private PendriveService pendriveService;
    private SavingPlacesService savingPlacesService;
    private AuthenticationService authenticationService;
    private RobotService robotService;

    private final static Logger logger = Logger.getLogger(OverallStatusController.class);

    @Autowired
    public OverallStatusController(DatabaseService databaseService, NetworkService networkService, PendriveService pendriveService, SavingPlacesService savingPlacesService, AuthenticationService authenticationService, RobotService robotService) {
        this.databaseService = databaseService;
        this.networkService = networkService;
        this.pendriveService = pendriveService;
        this.savingPlacesService = savingPlacesService;
        this.authenticationService = authenticationService;
        this.robotService = robotService;
    }

    @GetMapping("getAppStatus")
    public OveralStateDTO getAppStatus() throws IOException, InterruptedException {
        logger.info("Żądanie pobrania informacji o stanie serwera...");
        OveralStateDTO overalStateDTO = new OveralStateDTO();
        overalStateDTO.setDatabaseConnected(databaseService.getDatabaseSession() != null);
        SavingPlacesDTO savingPlacesDTO = savingPlacesService.getSavingPlacesDTO();
        overalStateDTO.setDatabaseEnabled(savingPlacesDTO.getJpgDatabaseSave() ||
                savingPlacesDTO.getMatDatabaseSave());
        overalStateDTO.setHotspotEnabled(networkService.getHotspotActive());
        overalStateDTO.setSecurityEnabled(authenticationService.getUsernameAndPasswordDTO().getEnabled());
        overalStateDTO.setJpgComputerSaveEnabled(savingPlacesDTO.getJpgComputerSave());
        overalStateDTO.setJpgLocation(savingPlacesDTO.getJpgComputerLocation());
        int cameras = 0;
        logger.info("Rozpoczynam wykrywanie podłączonych kamer...");
        if(new File("/dev/video0").exists())
            cameras++;
        if(new File("/dev/video1").exists())
            cameras++;
        logger.info("Liczba wykrytych kamer:"+cameras);
        overalStateDTO.setCameras(cameras);
        logger.info("Sprawdzam stan pendrive...");
        overalStateDTO.setPendriveEnabled(savingPlacesDTO.getMatPendriveSave() ||
                savingPlacesDTO.getJpgPendriveSave());
        overalStateDTO.setPendriveConnected(pendriveService.checkIfPendriveConnected());
        overalStateDTO.setPendriveMounted(pendriveService.checkWherePendriveMounted().isPresent());
        overalStateDTO.setRobotConnected(robotService.isRobotConnected());
        return overalStateDTO;
    }
}
