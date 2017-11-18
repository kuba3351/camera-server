package com.raspberry.camera.controller;

import com.raspberry.camera.dto.RobotIpDTO;
import com.raspberry.camera.service.ConfigFileService;
import com.raspberry.camera.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

@RestController
public class RobotController {

    private final RobotService robotService;

    @Autowired
    public RobotController(RobotService robotService) {
        this.robotService = robotService;
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
    public ResponseEntity connectToRobotWithIp(@RequestBody RobotIpDTO robotIpDTO) {
        try {
            robotService.connectToRobot(robotIpDTO.getIp());
            robotService.setRobotIp(robotIpDTO.getIp());
            return new ResponseEntity(HttpStatus.OK);
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}