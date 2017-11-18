package com.raspberry.camera.service;

import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.entity.Photo;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SavingPlacesService {

    private SavingPlacesDTO savingPlacesDTO;
    private DatabaseService databaseService;
    private PendriveService pendriveService;
    private ConfigFileService configFileService;

    public SavingPlacesDTO getSavingPlacesDTO() {
        return savingPlacesDTO;
    }

    public void setSavingPlacesDTO(SavingPlacesDTO savingPlacesDTO) throws IOException {
        this.savingPlacesDTO = savingPlacesDTO;
        configFileService.writeSavingPlaces(savingPlacesDTO);
    }

    @Autowired
    public SavingPlacesService(ConfigFileService configFileService, PendriveService pendriveService, DatabaseService databaseService) {
        this.configFileService = configFileService;
        this.savingPlacesDTO = configFileService.getSavingPlacesDTO();
        this.pendriveService = pendriveService;
        this.databaseService = databaseService;
    }

    public void saveJpgToDatabase(Photo photo1, Photo photo2) throws Exception {
        databaseService.saveJpgIntoDatabase(photo1, photo2);
    }

    public void saveMatToDatabase(Photo photo1, Photo photo2) throws Exception {
        databaseService.saveMatToDatabase(photo1 != null ? photo1.getMatImage() : null, photo2 != null ? photo2.getMatImage() : null);
    }

    public void saveJpgToPendrive(Photo photo1, Photo photo2) throws Exception {
        pendriveService.saveJpgToPendrive(photo1, photo2);
    }

    public void saveMatToPendrive(Photo photo1, Photo photo2) throws Exception {
        pendriveService.saveMatToPendrive(photo1, photo2);
    }
}
