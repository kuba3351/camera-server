package com.raspberry.camera.service;

import com.raspberry.camera.dto.AutoPhotosDTO;
import com.raspberry.camera.entity.RobotState;
import com.raspberry.camera.entity.ThreadState;
import lejos.hardware.Sound;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RobotService {

    private final static Logger logger = Logger.getLogger(RobotService.class);

    public RobotState getRobotState() {
        return robotState;
    }

    private RobotState robotState;
    private ConfigFileService configFileService;
    private RMIRegulatedMotor left;
    private RMIRegulatedMotor right;
    private RMIRegulatedMotor frontWheels;
    private RemoteEV3 robot;
    private static boolean robotConnected = false;
    private String robotIp;

    public RobotService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.robotIp = configFileService.readRobotIp();

        Thread thread = new Thread(() -> {
            try {
                connectToRobot();
                robotConnected = true;
                logger.info("Połączono z robotem.");
            } catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
                logger.warn("Błąd połączenia z robotem...");
            }
        });
        thread.start();
    }

    public String getRobotIp() {
        return robotIp;
    }

    public void setRobotIp(String robotIp) throws IOException {
        this.robotIp = robotIp;
        configFileService.writeRobotIP(robotIp);
    }

    public static boolean isRobotConnected() {
        return robotConnected;
    }

    public void connectToRobot() throws RemoteException, NotBoundException, MalformedURLException {
        connectToRobot(robotIp);
    }

    public void connectToRobot(String ip) throws RemoteException, NotBoundException, MalformedURLException {
        logger.info("Łączę z robotem...");
        robot = new RemoteEV3(ip);
        left = robot.createRegulatedMotor("A", 'L');
        right = robot.createRegulatedMotor("D", 'L');
        frontWheels = robot.createRegulatedMotor("frontWheels", 'L');
        robotState = RobotState.STOPPED;
        Sound.beep();
        Sound.buzz();
        robotIp = ip;
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
        frontWheels.stop(true);
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
        frontWheels.forward();
    }

    public void leftFinish() throws RemoteException {
        if (robotState.equals(RobotState.FORWARD))
            left.forward();
        else if (robotState.equals(RobotState.BACKWARD))
            left.backward();
    }

    public void goRight() throws RemoteException {
        frontWheels.backward();
    }

    public void rightFinish() throws RemoteException {
        if (robotState.equals(RobotState.FORWARD))
            right.forward();
        else if (robotState.equals(RobotState.BACKWARD))
            right.backward();
    }
}
