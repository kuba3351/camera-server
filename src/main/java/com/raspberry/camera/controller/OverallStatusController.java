package com.raspberry.camera.controller;

import com.raspberry.camera.dto.OveralStateDTO;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.service.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

import static com.raspberry.camera.service.RobotService.isRobotConnected;

/**
 * Kontroler odpowiedzialny za udostępnienie ogólnych informacji o systemie.
 */
@RestController
public class OverallStatusController {

    private final static Logger logger = Logger.getLogger(OverallStatusController.class);
    private DatabaseService databaseService;
    private NetworkService networkService;
    private PendriveService pendriveService;
    private SavingPlacesService savingPlacesService;
    private AuthenticationService authenticationService;
    private RobotService robotService;

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
    public ResponseEntity getAppStatus() {
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
        if (new File("/dev/video0").exists())
            cameras++;
        if (new File("/dev/video1").exists())
            cameras++;
        logger.info("Liczba wykrytych kamer:" + cameras);
        overalStateDTO.setCameras(cameras);
        logger.info("Sprawdzam stan pendrive...");
        overalStateDTO.setPendriveEnabled(savingPlacesDTO.getMatPendriveSave() ||
                savingPlacesDTO.getJpgPendriveSave());
        overalStateDTO.setPendriveConnected(pendriveService.checkIfPendriveConnected());
        try {
            overalStateDTO.setPendriveMounted(pendriveService.checkWherePendriveMounted().isPresent());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        overalStateDTO.setRobotConnected(isRobotConnected());
        overalStateDTO.setConnectRobotEnabled(RobotService.getRobotDTO().getConnect());
        return new ResponseEntity<>(overalStateDTO, HttpStatus.OK);
    }
}
