package com.raspberry.camera.service;

import java.rmi.RemoteException;

public class RabbitReceiver {

    private final RobotService robotService;

    public RabbitReceiver(RobotService robotService) {
        this.robotService = robotService;
    }

    public void receive(byte[] str) {
        String message = new String(str);
        handleMessage(message);
    }

    public void receive(String str) {
        handleMessage(str);
    }

    public void handleMessage(String message) {
        try {
            if (message.equals("up")) {
                robotService.goForward();
            }
            else if (message.equals("down")) {
                robotService.goBackward();
            }
            else if(message.equals("stop")) {
                robotService.stop();
            }
            else if(message.equals("left")) {
                robotService.goLeft();
            }
            else if(message.equals("right")) {
                robotService.goRight();
            }
            else if(message.equals("leftReleased")) {
                robotService.leftFinish();
            }
            else if(message.equals("rightReleased")) {
                robotService.rightFinish();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
