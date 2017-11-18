package com.raspberry.camera.controller;

import com.raspberry.camera.dto.PhotoResolutionDTO;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.entity.Photo;
import com.raspberry.camera.service.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by jakub on 29.07.17.
 */

@Controller
public class PhotoController {

    private final static Logger logger = Logger.getLogger(PhotoController.class);
    private PhotoService photoService;
    private SavingPlacesService savingPlacesService;
    private final int BUFFER_SIZE = 1000;
    Map<String, Photo> photos = new HashMap<>();

    @Autowired
    public PhotoController(PhotoService photoService, SavingPlacesService savingPlacesService) {
        this.photoService = photoService;
        this.savingPlacesService = savingPlacesService;
    }

    @GetMapping("/api/takePhoto")
    public void takePhoto(HttpServletResponse httpServletResponse) throws IOException, ExecutionException, InterruptedException {
        logger.info("odebrano żądanie wykonania zdjęcia...");
        ExecutorService service = Executors.newFixedThreadPool(4);
        Photo photo1 = new File("/dev/video0").exists() ? photoService.takePhoto(0) : null;
        Photo photo2 = new File("/dev/video1").exists() ? photoService.takePhoto(1) : null;
        photos.put("camera1", photo1);
        photos.put("camera2", photo2);
        makeZipArchive(httpServletResponse, photo1, photo2);
        SavingPlacesDTO savingPlacesDTO = savingPlacesService.getSavingPlacesDTO();
        if (savingPlacesDTO.getJpgDatabaseSave()) {
            service.submit(() -> {
                try {
                    savingPlacesService.saveJpgToDatabase(photo1, photo2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        if (savingPlacesDTO.getMatDatabaseSave()) {
            service.submit(() -> {
                try {
                    savingPlacesService.saveMatToDatabase(photo1, photo2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        if (savingPlacesDTO.getMatPendriveSave()) {
            service.submit(() -> {
                    try {
                        logger.info("Zapisuję macierz Mat na pendrive...");
                        savingPlacesService.saveMatToPendrive(photo1, photo2);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("Problem z zapisem macierzy Mat!");
                    }
            });
        }
        if (savingPlacesDTO.getJpgPendriveSave()) {
            service.submit(() -> {
                try {
                    logger.info("Zapisuję obraz jpg na pendrive...");
                    savingPlacesService.saveJpgToPendrive(photo1, photo2);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Problem z zapisem obrazu jpg.");
                }
            });
        }
    }

    private void makeZipArchive(HttpServletResponse httpServletResponse, Photo photo1, Photo photo2) throws IOException {
        ServletOutputStream httpServletResponseOutputStream = httpServletResponse.getOutputStream();
        ZipOutputStream zipOutputStream;
        if (httpServletResponseOutputStream != null)
            zipOutputStream = new ZipOutputStream(httpServletResponseOutputStream);
        else
            zipOutputStream = new ZipOutputStream(new OutputStream() {
                @Override
                public void write(int i) throws IOException {

                }
            });
        if (photo1 != null) {
            ZipEntry camera1 = new ZipEntry("camera1.jpg");
            zipOutputStream.putNextEntry(camera1);
            copyStream(new ByteArrayInputStream(photo1.getJpgImage()), zipOutputStream);
            zipOutputStream.closeEntry();
        }
        if (photo2 != null) {
            ZipEntry camera2 = new ZipEntry("camera2.jpg");
            zipOutputStream.putNextEntry(camera2);
            copyStream(new ByteArrayInputStream(photo2.getJpgImage()), zipOutputStream);
            zipOutputStream.closeEntry();
        }
        zipOutputStream.finish();
        zipOutputStream.close();
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        int numOfBytes;
        do {
            byte[] bytes = new byte[BUFFER_SIZE];
            numOfBytes = inputStream.read(bytes);
            outputStream.write(bytes);
        } while (numOfBytes != -1);
    }

    @GetMapping("/api/getLastPhotos")
    public void getLastPhoto(HttpServletResponse httpServletResponse) throws IOException {
        logger.info("Odebrano zapytanie o ostatnie zdjęcie...");
        makeZipArchive(httpServletResponse, photos.get("camera1"), photos.get("camera2"));
        logger.info("Przetworzono zapytanie o ostatnie zdjęcie...");
    }
}
