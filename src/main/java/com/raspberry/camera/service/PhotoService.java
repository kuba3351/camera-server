package com.raspberry.camera.service;

import com.raspberry.camera.dto.PhotoResolutionDTO;
import com.raspberry.camera.entity.Photo;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class PhotoService {

    private ConfigFileService configFileService;
    private PhotoResolutionDTO photoResolutionDTO;

    public PhotoResolutionDTO getPhotoResolutionDTO() {
        return photoResolutionDTO;
    }

    public void setPhotoResolutionDTO(PhotoResolutionDTO photoResolutionDTO) throws IOException {
        this.photoResolutionDTO = photoResolutionDTO;
        configFileService.savePhotoResolution(photoResolutionDTO);
    }

    @Autowired
    public PhotoService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.photoResolutionDTO = configFileService.getPhotoResolutionDTO();
    }

    public Photo takePhoto(int camera) throws FileNotFoundException {
        PhotoResolutionDTO photoResolutionDTO = configFileService.getPhotoResolutionDTO();
        VideoCapture capture = new VideoCapture(camera);
        capture.open(camera);
        Mat frame = new Mat();
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, photoResolutionDTO.getWidth());
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, photoResolutionDTO.getHeigth());
        capture.read(frame);
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", frame, matOfByte);
        capture.release();
        return new Photo(frame, matOfByte.toArray());
    }
}