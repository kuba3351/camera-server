package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jakub on 17.08.17.
 */
@RestController
public class SavingPlacesController {

    private final static Logger logger = Logger.getLogger(SavingPlacesController.class);

    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/api/savingPlaces")
    public SavingPlacesDTO getSavingPlaces() {
        return configFileService.getSavingPlacesDTO();
    }

    @PostMapping("/api/savingPlaces")
    public ResponseEntity updateSavingPlacesConfig(@RequestBody SavingPlacesDTO savingPlacesDTO) {
        try {
            logger.info("Uaktualniam konfigurację miejsc zapisu...");
            databaseService.setUpDatabaseSession(savingPlacesDTO.getDatabaseConfig());
            configFileService.writeSavingPlaces(savingPlacesDTO);
            logger.info("Uaktualnianie akończone...");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
