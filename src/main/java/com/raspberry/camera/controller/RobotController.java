package com.raspberry.camera.controller;

import com.raspberry.camera.dto.AutoPhotosDTO;
import com.raspberry.camera.dto.RobotIpDTO;
import com.raspberry.camera.service.RobotService;
import com.raspberry.camera.service.SensorDistanceListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

@RestController
public class RobotController {

    private final RobotService robotService;
    private SensorDistanceListenerService sensorDistanceListenerService;

    @Autowired
    public RobotController(RobotService robotService, SensorDistanceListenerService sensorDistanceListenerService) {
        this.robotService = robotService;
        this.sensorDistanceListenerService = sensorDistanceListenerService;
    }

    @GetMapping("/api/robot/connectToRobot")
    public ResponseEntity connectToRobot() {
        try {
            robotService.connectToRobot();
            return new ResponseEntity(HttpStatus.OK);
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/robot/connectToRobot")
    public ResponseEntity connectToRobotWithIp(@RequestBody @Valid RobotIpDTO robotIpDTO) {
        try {
            robotService.connectToRobot(robotIpDTO.getIp());
            robotService.setRobotIp(robotIpDTO.getIp());
            if(sensorDistanceListenerService.getAutoPhotosDTO().getAutoPhotosEnabled()) {
                sensorDistanceListenerService.startListener();
            }
            return new ResponseEntity(HttpStatus.OK);
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/robot/autoPhotos")
    public AutoPhotosDTO getAutoPhotos() {
        return sensorDistanceListenerService.getAutoPhotosDTO();
    }

    @PostMapping("/api/robot/autoPhotos")
    public ResponseEntity setAutoPhotos(@RequestBody AutoPhotosDTO autoPhotosDTO) {
        try {
            sensorDistanceListenerService.setAutoPhotosDTO(autoPhotosDTO);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}