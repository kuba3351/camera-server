package com.raspberry.camera.service;

import com.raspberry.camera.MatUtils;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.other.MatContainer;
import com.raspberry.camera.other.Photo;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Serwis służący do zarządzania ustawieniami miejsc zapisu
 */
@Service
public class SavingPlacesService {

    private SavingPlacesDTO savingPlacesDTO;
    private DatabaseService databaseService;
    private PendriveService pendriveService;
    private ConfigFileService configFileService;

    @Autowired
    public SavingPlacesService(ConfigFileService configFileService, PendriveService pendriveService, DatabaseService databaseService) {
        this.configFileService = configFileService;
        this.savingPlacesDTO = configFileService.getSavingPlacesDTO();
        this.pendriveService = pendriveService;
        this.databaseService = databaseService;
    }

    public SavingPlacesDTO getSavingPlacesDTO() {
        return savingPlacesDTO;
    }

    public void setSavingPlacesDTO(SavingPlacesDTO savingPlacesDTO) throws IOException {
        this.savingPlacesDTO = savingPlacesDTO;
        configFileService.writeSavingPlaces(savingPlacesDTO);
    }

    public void saveJpgToDatabase(Photo photo1, Photo photo2) throws Exception {
        databaseService.saveJpgIntoDatabase(photo1, photo2);
    }

    public void saveMatToDatabase(Photo photo1, Photo photo2) throws Exception {
        MatContainer container1 = new MatContainer();
        Mat matImage1 = photo1.getMatImage();
        container1.setRows(matImage1.rows());
        container1.setCols(matImage1.cols());
        container1.setData(MatUtils.extractDataFromMat(matImage1));
        MatContainer container2 = new MatContainer();
        Mat matImage2 = photo2.getMatImage();
        container2.setRows(matImage2.rows());
        container2.setCols(matImage2.cols());
        container2.setData(MatUtils.extractDataFromMat(matImage2));
        databaseService.saveMatFromMatContainer(container1, container2);
    }

    public void saveJpgToPendrive(Photo photo1, Photo photo2) throws Exception {
        pendriveService.saveJpgToPendrive(photo1, photo2);
    }

    public void saveMatToPendrive(Photo photo1, Photo photo2) throws Exception {
        pendriveService.saveMatToPendrive(photo1, photo2);
    }
}
