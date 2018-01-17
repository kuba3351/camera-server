package com.raspberry.camera.service;

import com.raspberry.camera.dto.RobotDTO;
import com.raspberry.camera.other.RobotState;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RobotService {

    private final static Logger logger = Logger.getLogger(RobotService.class);
    private static RobotState robotState;
    private static RobotDTO robotDTO;
    private static volatile boolean robotConnected = false;
    private ConfigFileService configFileService;
    private RMIRegulatedMotor left;
    private RMIRegulatedMotor right;
    private RemoteEV3 robot;
    private String robotIp;

    public RobotService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.robotIp = configFileService.readRobotIp();
        robotDTO = configFileService.getRobotDTO();
        Thread thread = new Thread(() -> {
            try {
                connectToRobot();
                logger.info("Połączono z robotem.");
            } catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
                logger.warn("Błąd połączenia z robotem...");
            }
        });
        if(robotDTO.getConnect())
            thread.start();
    }

    public static RobotState getRobotState() {
        return robotState;
    }

    public static void setRobotState(RobotState robotState) {
        RobotService.robotState = robotState;
    }

    public static RobotDTO getRobotDTO() {
        return robotDTO;
    }

    public void setRobotDTO(RobotDTO robotDTO) throws IOException {
        RobotService.robotDTO = robotDTO;
        configFileService.writeRobotDto(robotDTO);
    }

    public static boolean isRobotConnected() {
        return robotConnected;
    }

    public String getRobotIp() {
        return robotIp;
    }

    public void setRobotIp(String robotIp) throws IOException {
        this.robotIp = robotIp;
        configFileService.writeRobotIP(robotIp);
    }

    public void connectToRobot() throws RemoteException, NotBoundException, MalformedURLException {
        connectToRobot(robotIp);
    }

    public void connectToRobot(String ip) throws RemoteException, NotBoundException, MalformedURLException {
        logger.info("Łączę z robotem...");
        robot = new RemoteEV3(ip);
        left = robot.createRegulatedMotor(robotDTO.getLeft(), 'L');
        right = robot.createRegulatedMotor(robotDTO.getRight(), 'L');
        robotState = RobotState.STOPPED;
        robotIp = ip;
        robotConnected = true;
    }

    public void goForward() throws RemoteException {
        left.setAcceleration(400);
        left.setSpeed(400);
        left.forward();
        right.setAcceleration(400);
        right.setSpeed(400);
        right.forward();
        robotState = RobotState.FORWARD;
    }

    public void stop() throws RemoteException {
        left.stop(true);
        right.stop(true);
        robotState = RobotState.STOPPED;
    }

    public void goBackward() throws RemoteException {
        left.setAcceleration(400);
        left.setSpeed(400);
        left.backward();
        right.setAcceleration(400);
        right.setSpeed(400);
        right.backward();
        robotState = RobotState.BACKWARD;
    }

    public void goLeft() throws RemoteException {
        left.stop(true);
    }

    public void leftFinish() throws RemoteException {
        if (robotState.equals(RobotState.FORWARD))
            left.forward();
        else if (robotState.equals(RobotState.BACKWARD))
            left.backward();
    }

    public void goRight() throws RemoteException {
        right.stop(true);
    }

    public void rightFinish() throws RemoteException {
        if (robotState.equals(RobotState.FORWARD))
            right.forward();
        else if (robotState.equals(RobotState.BACKWARD))
            right.backward();
    }
}
