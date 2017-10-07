package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;

@Component
public class PhotoService {

    @Autowired
    private DatabaseService databaseService;

    public static final String FILE = "/tmp/obraz.jpg";
    private static VideoCapture capture;

    private final static Logger logger = Logger.getLogger(PhotoService.class);

    public File takePhoto(SavingPlacesDTO savingPlacesDTO) throws FileNotFoundException {
        Mat frame = new Mat();
        if(capture == null)
            initialize();
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 1920);
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 1080);
        capture.read(frame);
        Highgui.imwrite(FILE, frame);
        File file = new File(FILE);
        if(savingPlacesDTO.getJpgDatabaseSave()) {
            Thread thread = new Thread(() -> {
                logger.info("Zapisuję jpg do bazy...");
                try {
                    databaseService.saveJpgIntoDatabase(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("Zapis jpg do bazy ukończony...");
            });
            thread.start();
        }
        if(savingPlacesDTO.getMatDatabaseSave()) {
            Thread thread = new Thread(() -> {
                logger.info("Zapisuję Mat do bazy...");
                try {
                    databaseService.saveMatToDatabase(frame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("Zapis Mat do bazy ukończony...");
            });
            thread.start();
        }
        return file;
    }

    public static void takeFirstPhoto() {
        Mat frame = new Mat();
        if(capture == null)
            initialize();
        capture.read(frame);
    }

    public static void release() {
        capture.release();
        capture = null;
    }

    public static void initialize() {
        capture = new VideoCapture(0);
        capture.open(0);
    }
}