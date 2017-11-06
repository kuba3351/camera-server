package com.raspberry.camera.service;

import com.raspberry.camera.entity.Photo;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;

@Component
public class PhotoService {

    private final static Logger logger = Logger.getLogger(PhotoService.class);

    private int cameras;

    public int getCameras() {
        return cameras;
    }

    public PhotoService() {
        cameras = 0;
        logger.info("Rozpoczynam wykrywanie podłączonych kamer...");
        if(new File("/dev/video0").exists())
            cameras++;
        if(new File("/dev/video1").exists())
            cameras++;
        logger.info("Liczba wykrytych kamer:"+cameras);
    }

    public Photo takePhoto(int camera) throws FileNotFoundException {
        VideoCapture capture = new VideoCapture(camera);
        capture.open(camera);
        Mat frame = new Mat();
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 1280);
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 720);
        capture.read(frame);
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", frame, matOfByte);
        capture.release();
        return new Photo(frame, matOfByte.toArray());
    }
}