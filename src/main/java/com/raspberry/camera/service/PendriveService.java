package com.raspberry.camera.service;

import com.raspberry.camera.MatUtils;
import com.raspberry.camera.entity.MatEntity;
import com.raspberry.camera.entity.Photo;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
        if(configFileService.getSavingPlacesDTO().getJpgPendriveSave() || configFileService.getSavingPlacesDTO().getMatPendriveSave()) {
            logger.info("Sprawdzam pendrive...");
            if(checkIfPendriveConnected()) {
                logger.info("Pendrive podłączony. Sprawdzam montowanie...");
                try {
                    if(checkWherePendriveMounted().isPresent()) {
                        logger.info("Pendrive już zamontowany. Kontynuuję uruchomienie...");
                    }
                    else {
                        logger.info("Montuję pendrive...");
                        if(mountPendrive()) {
                            logger.info("Pendrive zamontowany. Kontynuuję uruchomienie...");
                        }
                        else logger.error("Problem z montowaniem pendrive...");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else logger.warn("Pendrive nie jest podłączony!");
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
            File photo = new File(jpgPath.toString() + "/" + LocalDateTime.now() + "-camera1.jpg");
            photo.createNewFile();
            FileWriter writer = new FileWriter(photo);
            writer.write(new String(photo1.getJpgImage()));
            writer.close();
        }
        if (photo2 != null) {
            FileWriter writer = new FileWriter(new File(jpgPath.toString()+"/"+LocalDateTime.now()+"-camera2.jpg"));
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
            File matFile = new File(matPath.toString() + "/" + LocalDateTime.now() + "-camera1.yml");
            matFile.createNewFile();
            FileWriter fileWriter = new FileWriter(matFile);
            MatEntity matEntity = new MatEntity();
            Mat mat = photo1.getMatImage();
            matEntity.setCols(mat.cols());
            matEntity.setRows(mat.rows());
            matEntity.setData(MatUtils.extractDataFromMat(mat));
            ByteArrayOutputStream outputStream = MatUtils.writeMat(matEntity);
            fileWriter.write(new String(outputStream.toByteArray()));
            fileWriter.close();
        }
        if (photo2 != null) {
            FileWriter fileWriter = new FileWriter(new File(matPath.toString() + "/" + LocalDateTime.now() + "-camera2.yml"));
            MatEntity matEntity = new MatEntity();
            Mat mat = photo2.getMatImage();
            matEntity.setCols(mat.cols());
            matEntity.setRows(mat.rows());
            matEntity.setData(MatUtils.extractDataFromMat(mat));
            ByteArrayOutputStream outputStream = MatUtils.writeMat(matEntity);
            fileWriter.write(new String(outputStream.toByteArray()));
            fileWriter.close();
        }
    }
}
