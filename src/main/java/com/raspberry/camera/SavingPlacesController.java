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
        SavingPlacesDTO outputDto = new SavingPlacesDTO();
        DatabaseConfigDTO outputDatabaseConfig = new DatabaseConfigDTO();
        SavingPlacesDTO savingPlacesDTO = configFileService.getSavingPlacesDTO();
        DatabaseConfigDTO databaseConfigDTO = savingPlacesDTO.getDatabaseConfig();
        outputDto.setDatabaseConfig(outputDatabaseConfig);
        outputDto.setJpgComputerSave(savingPlacesDTO.getJpgComputerSave());
        outputDto.setJpgComputerLocation(savingPlacesDTO.getJpgComputerLocation());
        outputDto.setJpgDatabaseSave(savingPlacesDTO.getJpgDatabaseSave());
        outputDto.setJpgRaspberryPendriveSave(savingPlacesDTO.getJpgRaspberryPendriveSave());
        outputDto.setMatDatabaseSave(savingPlacesDTO.getMatDatabaseSave());
        outputDto.setMatPendriveSave(savingPlacesDTO.getMatPendriveSave());
        outputDatabaseConfig.setHost(databaseConfigDTO.getHost());
        outputDatabaseConfig.setPort(databaseConfigDTO.getPort());
        outputDatabaseConfig.setDatabaseType(databaseConfigDTO.getDatabaseType());
        outputDatabaseConfig.setDatabaseName(databaseConfigDTO.getDatabaseName());
        outputDatabaseConfig.setUser(databaseConfigDTO.getUser());
        return outputDto;
    }

    @PostMapping("/api/savingPlaces")
    public ResponseEntity updateSavingPlacesConfig(@RequestBody SavingPlacesDTO savingPlacesDTO) {
        try {
            logger.info("Uaktualniam konfigurację miejsc zapisu...");
            String password = savingPlacesDTO.getDatabaseConfig().getPassword();
            if(password == null) {
                savingPlacesDTO.getDatabaseConfig()
                        .setPassword(configFileService.getSavingPlacesDTO()
                                .getDatabaseConfig().getPassword());
            }
            if(savingPlacesDTO.getJpgDatabaseSave() || savingPlacesDTO.getMatDatabaseSave())
                databaseService.setUpDatabaseSession(savingPlacesDTO.getDatabaseConfig());
            configFileService.writeSavingPlaces(savingPlacesDTO);
            logger.info("Uaktualnianie akończone...");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("api/connectToDatabase")
    public ResponseEntity bringUpDatabaseConnection() {
        try {
            databaseService.setUpDatabaseSession(configFileService.getSavingPlacesDTO().getDatabaseConfig());
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }
}
