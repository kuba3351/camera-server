package com.raspberry.camera.controller;

import com.raspberry.camera.FakeOutputStream;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.entity.Photo;
import com.raspberry.camera.entity.RobotState;
import com.raspberry.camera.service.PhotoService;
import com.raspberry.camera.service.RabbitSender;
import com.raspberry.camera.service.RobotService;
import com.raspberry.camera.service.SavingPlacesService;
import org.apache.log4j.Logger;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
 * Kontroler odpowiedzialny za robienie zdjęć
 */
@Controller
public class PhotoController {

    private final static Logger logger = Logger.getLogger(PhotoController.class);
    private final int BUFFER_SIZE = 1000;
    private Map<String, Photo> photos = new HashMap<>();
    private PhotoService photoService;
    private SavingPlacesService savingPlacesService;
    private RabbitSender rabbitSender;

    @Autowired
    public PhotoController(PhotoService photoService, SavingPlacesService savingPlacesService, RabbitSender rabbitSender) {
        this.photoService = photoService;
        this.savingPlacesService = savingPlacesService;
        this.rabbitSender = rabbitSender;
    }

    /**
     * Obsługa żądania zrobienia nowych zdjęć. Zwraca je w archiwum ZIP
     *
     * @param httpServletResponse
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/api/takePhoto")
    public void takePhoto(HttpServletResponse httpServletResponse) throws IOException, ExecutionException, InterruptedException {
        logger.info("odebrano żądanie wykonania zdjęcia...");
        ExecutorService service = Executors.newFixedThreadPool(4);
        photoService.takePhotos();
        Photo photo1 = photoService.getPhoto1();
        generateJpg(photo1);
        Photo photo2 = photoService.getPhoto2();
        generateJpg(photo2);
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
                    rabbitSender.send("[ERROR]Błąd zapisu obrazu JPG do bazy");
                }
            });
        }
        if (savingPlacesDTO.getMatDatabaseSave()) {
            service.submit(() -> {
                try {
                    savingPlacesService.saveMatToDatabase(photo1, photo2);
                } catch (Exception e) {
                    e.printStackTrace();
                    rabbitSender.send("[ERROR]Błąd zapisu macierzy Mat do bazy");
                }
            });
        }
        if (savingPlacesDTO.getMatPendriveSave()) {
            service.submit(() -> {
                try {
                    logger.info("Zapisuję macierz Mat na pendrive...");
                    savingPlacesService.saveMatToPendrive(photo1, photo2);
                    logger.info("Macierz Mat zapisana na pendrive");
                } catch (Exception e) {
                    e.printStackTrace();
                    rabbitSender.send("[ERROR]Błąd zapisu macierzy Mat na pendrive");
                    logger.error("Problem z zapisem macierzy Mat!");
                }
            });
        }
        if (savingPlacesDTO.getJpgPendriveSave()) {
            service.submit(() -> {
                try {
                    logger.info("Zapisuję obraz jpg na pendrive...");
                    savingPlacesService.saveJpgToPendrive(photo1, photo2);
                    logger.info("Obraz jpg zapisany na pendrive");
                } catch (Exception e) {
                    e.printStackTrace();
                    rabbitSender.send("[ERROR]Błąd zapisu obrazu JPG na pendrive");
                    logger.error("Problem z zapisem obrazu jpg.");
                }
            });
        }
    }

    private void generateJpg(Photo photo1) {
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", photo1.getMatImage(), matOfByte);
        photo1.setJpgImage(matOfByte.toArray());
    }

    private void makeZipArchive(HttpServletResponse httpServletResponse, Photo photo1, Photo photo2) throws IOException {
        ServletOutputStream httpServletResponseOutputStream = httpServletResponse.getOutputStream();
        ZipOutputStream zipOutputStream;
        if (httpServletResponseOutputStream != null)
            zipOutputStream = new ZipOutputStream(httpServletResponseOutputStream);
        else
            zipOutputStream = new ZipOutputStream(new FakeOutputStream());
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

    /**
     * Pobieranie ostatnio zrobionych zdjęć w arhiwum ZIP
     *
     * @param httpServletResponse
     * @throws IOException
     */
    @GetMapping("/api/getLastPhotos")
    public void getLastPhoto(HttpServletResponse httpServletResponse) throws IOException {
        logger.info("Odebrano zapytanie o ostatnie zdjęcie...");
        makeZipArchive(httpServletResponse, photos.get("camera1"), photos.get("camera2"));
        logger.info("Przetworzono zapytanie o ostatnie zdjęcie...");
    }

}
