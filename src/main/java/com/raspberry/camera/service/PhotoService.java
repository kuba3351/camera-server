package com.raspberry.camera.service;

import com.raspberry.camera.dto.PhotoResolutionDTO;
import com.raspberry.camera.entity.Photo;
import com.raspberry.camera.entity.RobotState;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

@Service
public class PhotoService {

    private ConfigFileService configFileService;
    private PhotoResolutionDTO photoResolutionDTO;
    private RobotService robotService;
    private RobotState robotState;
    private Photo photo1;
    private Photo photo2;

    public Photo getPhoto1() {
        return photo1;
    }

    public Photo getPhoto2() {
        return photo2;
    }

    @Autowired
    public PhotoService(ConfigFileService configFileService, RobotService robotService) {
        this.configFileService = configFileService;
        this.photoResolutionDTO = configFileService.getPhotoResolutionDTO();
        this.robotService = robotService;
    }

    public PhotoResolutionDTO getPhotoResolutionDTO() {
        return photoResolutionDTO;
    }

    public void setPhotoResolutionDTO(PhotoResolutionDTO photoResolutionDTO) throws IOException {
        this.photoResolutionDTO = photoResolutionDTO;
        configFileService.writePhotoResolution(photoResolutionDTO);
    }

    public void takePhotos() throws FileNotFoundException, RemoteException {
        if(robotService.isRobotConnected()) {
            robotState = robotService.getRobotState();
            robotService.stop();
        }
        photo1 = new File("/dev/video0").exists() ? takePhoto(0) : null;
        photo2 = new File("/dev/video1").exists() ? takePhoto(1) : null;
        if(robotState != null) {
            if(robotState.equals(RobotState.FORWARD))
                robotService.goForward();
            else if(robotState.equals(RobotState.BACKWARD))
                robotService.goBackward();
        }
    }

    private Photo takePhoto(int camera) throws FileNotFoundException {
        PhotoResolutionDTO photoResolutionDTO = configFileService.getPhotoResolutionDTO();
        VideoCapture capture = new VideoCapture(camera);
        capture.open(camera);
        Mat frame = new Mat();
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, photoResolutionDTO.getWidth());
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, photoResolutionDTO.getHeigth());
        capture.read(frame);
        capture.release();
        return new Photo(frame);
    }
}