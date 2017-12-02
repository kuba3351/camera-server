package com.raspberry.camera.controller;

import com.raspberry.camera.dto.DatabaseConfigDTO;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.service.DatabaseService;
import com.raspberry.camera.service.SavingPlacesService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Kontroler odpowiedzialny za ustawienia miejsc zapisu
 */
@RestController
public class SavingPlacesController {

    private final static Logger logger = Logger.getLogger(SavingPlacesController.class);

    private DatabaseService databaseService;
    private SavingPlacesService savingPlacesService;

    @Autowired
    public SavingPlacesController(DatabaseService databaseService, SavingPlacesService savingPlacesService) {
        this.databaseService = databaseService;
        this.savingPlacesService = savingPlacesService;
    }

    @GetMapping("/api/savingPlaces")
    public SavingPlacesDTO getSavingPlaces() {
        logger.info("Żądanie pobrania konfiguracji miejsc zapisu...");
        SavingPlacesDTO outputDto = new SavingPlacesDTO();
        DatabaseConfigDTO outputDatabaseConfig = new DatabaseConfigDTO();
        SavingPlacesDTO savingPlacesDTO = savingPlacesService.getSavingPlacesDTO();
        DatabaseConfigDTO databaseConfigDTO = savingPlacesDTO.getDatabaseConfig();
        outputDto.setDatabaseConfig(outputDatabaseConfig);
        outputDto.setJpgComputerSave(savingPlacesDTO.getJpgComputerSave());
        outputDto.setJpgComputerLocation(savingPlacesDTO.getJpgComputerLocation());
        outputDto.setJpgDatabaseSave(savingPlacesDTO.getJpgDatabaseSave());
        outputDto.setJpgPendriveSave(savingPlacesDTO.getJpgPendriveSave());
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
    public ResponseEntity updateSavingPlacesConfig(@RequestBody @Valid SavingPlacesDTO savingPlacesDTO) {
        try {
            logger.info("Uaktualniam konfigurację miejsc zapisu...");
            String password = savingPlacesDTO.getDatabaseConfig().getPassword();
            if (password == null) {
                savingPlacesDTO.getDatabaseConfig()
                        .setPassword(savingPlacesService.getSavingPlacesDTO()
                                .getDatabaseConfig().getPassword());
            }
            if (savingPlacesDTO.getJpgDatabaseSave() || savingPlacesDTO.getMatDatabaseSave())
                databaseService.setUpDatabaseSession(savingPlacesDTO.getDatabaseConfig());
            savingPlacesService.setSavingPlacesDTO(savingPlacesDTO);
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
            databaseService.setUpDatabaseSession(savingPlacesService.getSavingPlacesDTO().getDatabaseConfig());
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }
}
