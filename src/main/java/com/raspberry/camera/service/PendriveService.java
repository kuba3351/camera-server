package com.raspberry.camera.service;

import com.raspberry.camera.MatUtils;
import com.raspberry.camera.other.MatContainer;
import com.raspberry.camera.other.Photo;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
public class PendriveService {

    private final static Logger logger = Logger.getLogger(PendriveService.class);

    @Autowired
    public PendriveService(ConfigFileService configFileService) {
        if (configFileService.getSavingPlacesDTO().getJpgPendriveSave() || configFileService.getSavingPlacesDTO().getMatPendriveSave()) {
            Thread thread = new Thread(() -> {
                logger.info("Sprawdzam pendrive...");
                if (checkIfPendriveConnected()) {
                    logger.info("Pendrive podłączony. Sprawdzam montowanie...");
                    try {
                        if (checkWherePendriveMounted().isPresent()) {
                            logger.info("Pendrive już zamontowany. Kontynuuję uruchomienie...");
                        } else {
                            logger.info("Montuję pendrive...");
                            if (mountPendrive()) {
                                logger.info("Pendrive zamontowany. Kontynuuję uruchomienie...");
                            } else logger.error("Problem z montowaniem pendrive...");
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else logger.warn("Pendrive nie jest podłączony!");
            });
            thread.start();
        }
    }

    public boolean checkIfPendriveConnected() {
        return new File("/dev/sda").exists();
    }

    public Optional<String> checkWherePendriveMounted() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("mount");
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().filter(line -> line.startsWith("/dev/sda"))
                .map(line -> new ArrayList<>(Arrays.asList(line.split(" "))))
                .map(list -> list.get(2)).findFirst();
    }

    public boolean mountPendrive() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("sudo mount /dev/sda /home/pi/pendrive");
        return process.waitFor() == 0;
    }

    public void saveJpgToPendrive(Photo photo1, Photo photo2) throws Exception {
        String path = checkWherePendriveMounted().orElseThrow(() -> new Exception("Nie można znaleźć pendrive"));
        File jpgPath = new File(path + "/jpg");
        if (!jpgPath.isDirectory())
            jpgPath.mkdir();
        if (photo1 != null) {
            String jpgFilePath = jpgPath.toString() + "/" + LocalDateTime.now() + "-camera1.jpg";
            File photo = new File(jpgFilePath.replace(":", "-"));
            photo.createNewFile();
            FileWriter writer = new FileWriter(photo);
            writer.write(new String(photo1.getJpgImage()));
            writer.close();
        }
        if (photo2 != null) {
            String jpgFilePath = jpgPath.toString() + "/" + LocalDateTime.now() + "-camera2.jpg";
            FileWriter writer = new FileWriter(new File(jpgFilePath.replace(":", "-")));
            writer.write(new String(photo2.getJpgImage()));
            writer.close();
        }
    }

    public void saveMatToPendrive(Photo photo1, Photo photo2) throws Exception {
        String path = checkWherePendriveMounted().orElseThrow(() -> new Exception("Nie można znaleźć pendrive"));
        File matPath = new File(path + "/mat");
        if (!matPath.isDirectory())
            matPath.mkdir();
        if (photo1 != null) {
            String matFilePath = matPath.toString() + "/" + LocalDateTime.now() + "-camera1.yml";
            File matFile = new File(matFilePath.replace(":", "-"));
            matFile.createNewFile();
            FileWriter fileWriter = new FileWriter(matFile);
            MatContainer matContainer = new MatContainer();
            Mat mat = photo1.getMatImage();
            matContainer.setCols(mat.cols());
            matContainer.setRows(mat.rows());
            matContainer.setData(MatUtils.extractDataFromMat(mat));
            ByteArrayOutputStream outputStream = MatUtils.writeMat(matContainer);
            fileWriter.write(new String(outputStream.toByteArray()));
            fileWriter.close();
        }
        if (photo2 != null) {
            String matFilePath = matPath.toString() + "/" + LocalDateTime.now() + "-camera2.yml";
            FileWriter fileWriter = new FileWriter(new File(matFilePath.replace(":", "-")));
            MatContainer matContainer = new MatContainer();
            Mat mat = photo2.getMatImage();
            matContainer.setCols(mat.cols());
            matContainer.setRows(mat.rows());
            matContainer.setData(MatUtils.extractDataFromMat(mat));
            ByteArrayOutputStream outputStream = MatUtils.writeMat(matContainer);
            fileWriter.write(new String(outputStream.toByteArray()));
            fileWriter.close();
        }
    }
}
