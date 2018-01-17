package com.raspberry.camera.service;

import com.raspberry.camera.dto.PhotoDTO;
import com.raspberry.camera.other.CameraType;
import com.raspberry.camera.other.Photo;
import com.raspberry.camera.other.RobotState;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
public class PhotoService {

    private ConfigFileService configFileService;
    private PhotoDTO photoDTO;
    private RobotService robotService;
    private RobotState robotState;

    @Autowired
    public PhotoService(ConfigFileService configFileService, RobotService robotService) {
        this.configFileService = configFileService;
        this.photoDTO = configFileService.getPhotoDTO();
        this.robotService = robotService;
    }

    public PhotoDTO getPhotoDTO() {
        return photoDTO;
    }

    public void setPhotoDTO(PhotoDTO photoDTO) throws IOException {
        this.photoDTO = photoDTO;
        configFileService.writePhotoDTO(photoDTO);
    }

    public Map<Integer, Photo> takePhotos() throws Exception {
        if(RobotService.isRobotConnected() && RobotService.getRobotDTO().getShouldStopOnPhotos()) {
            robotState = RobotService.getRobotState();
            robotService.stop();
        }
        Map<Integer, Photo> photoMap = null;
        if(photoDTO.getCameraType().equals(CameraType.USB)) {
            Photo photo1 = new File("/dev/video0").exists() ? takePhoto(0) : null;
            Photo photo2 = new File("/dev/video1").exists() ? takePhoto(1) : null;
            photoMap = new HashMap<>();
            photoMap.put(1, photo1);
            photoMap.put(2, photo2);
        }
        if(photoDTO.getCameraType().equals(CameraType.RASPBERRY))
            photoMap = takePhotosFromPython();
        if(robotState != null) {
            if(robotState.equals(RobotState.FORWARD))
                robotService.goForward();
            else if(robotState.equals(RobotState.BACKWARD))
                robotService.goBackward();
        }
        return photoMap;
    }

    private Photo takePhoto(int camera) throws FileNotFoundException {
        PhotoDTO photoDTO = configFileService.getPhotoDTO();
        VideoCapture capture = new VideoCapture(camera);
        capture.open(camera);
        Mat frame = new Mat();
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, photoDTO.getWidth());
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, photoDTO.getHeigth());
        capture.read(frame);
        capture.release();
        MatOfByte buffer = new MatOfByte();
        Highgui.imencode(".jpg", frame, buffer);
        return new Photo(frame, buffer.toArray());
    }

    public Map<Integer, Photo> takePhotosFromPython() throws Exception {
        if(Runtime.getRuntime().exec("python /home/pi/pythonscript.py "+photoDTO.getWidth()+" "+photoDTO.getHeigth()).waitFor() != 0)
            throw new Exception("Problem podczas robienia zdjęć");
        Map<Integer, Photo> photos = new HashMap<>();
        String file1 = "./capture_1.jpg";
        Mat mat = Highgui.imread("/capture_1.jpg");
        photos.put(1, new Photo(mat, Files.readAllBytes(new File(file1).toPath())));
        String file2 = "./capture_2.jpg";
        Mat mat2 = Highgui.imread("/capture_2.jpg");
        photos.put(2, new Photo(mat2, Files.readAllBytes(new File(file2).toPath())));
        return photos;
    }
}