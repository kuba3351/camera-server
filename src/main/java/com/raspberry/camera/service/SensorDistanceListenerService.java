package com.raspberry.camera.service;

import com.raspberry.camera.FakeHttpServletResponse;
import com.raspberry.camera.controller.PhotoController;
import com.raspberry.camera.dto.AutoPhotosDTO;
import com.raspberry.camera.other.RobotState;
import com.raspberry.camera.other.ThreadState;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SensorDistanceListenerService implements Runnable {

    private final static Logger logger = Logger.getLogger(SensorDistanceListenerService.class);

    private final RabbitSender rabbitSender;
    private final PhotoController photoController;
    private ThreadState autophotosThreadState;

    public AutoPhotosDTO getAutoPhotosDTO() {
        return autoPhotosDTO;
    }

    private AutoPhotosDTO autoPhotosDTO;
    private Thread autoPhotosThread;
    private ConfigFileService configFileService;

    @Autowired
    public SensorDistanceListenerService(RabbitSender rabbitSender, PhotoController photoController, ConfigFileService configFileService) {
        this.rabbitSender = rabbitSender;
        this.configFileService = configFileService;
        this.photoController = photoController;
        this.autoPhotosDTO = configFileService.getAutoPhotosDTO();
        autoPhotosThread = new Thread(this);
        autoPhotosThread.start();
        autophotosThreadState = ThreadState.RUNNING;
    }

    @Override
    public void run() {
        while (!RobotService.isRobotConnected() || !autoPhotosDTO.getAutoPhotosEnabled()) {

        }
        logger.info("Uruchamiam sensor odległości...");
        EV3UltrasonicSensor sensor = null;
        switch (RobotService.getRobotDTO().getDistanceSensor()) {
            case "S1":
                sensor = new EV3UltrasonicSensor(SensorPort.S1);
                break;
            case "S2":
                sensor = new EV3UltrasonicSensor(SensorPort.S2);
                break;
            case "S3":
                sensor = new EV3UltrasonicSensor(SensorPort.S3);
                break;
            case "S4":
                sensor = new EV3UltrasonicSensor(SensorPort.S4);
                break;
        }
        SampleProvider provider = sensor.getDistanceMode();
        float[] sample = new float[provider.sampleSize()];
        boolean photoTaken = false;
        int reads = 0;
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            provider.fetchSample(sample, 0);
            float abs = Math.abs((sample[0] * 100f) - (float)autoPhotosDTO.getAutoPhotosDistance());
            if (abs <= 50) {
                reads++;
                if (!photoTaken && reads == 3) {
                    try {
                        rabbitSender.send("Taking photo...");
                        RobotService.setRobotState(RobotState.STOPPED);
                        photoController.takePhoto(new FakeHttpServletResponse());
                        rabbitSender.send("Photo taken!");
                        Thread.sleep(3000);
                        photoTaken = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                photoTaken = false;
                reads = 0;
            }
        }
    }

    public void setAutoPhotosDTO(AutoPhotosDTO autoPhotosDTO) throws IOException {
        configFileService.writeAutophotosDTO(autoPhotosDTO);
        if(autophotosThreadState.equals(ThreadState.SUSPENDED) && autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.resume();
            autophotosThreadState = ThreadState.RUNNING;
        }
        if(autophotosThreadState.equals(ThreadState.RUNNING) && !autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.suspend();
            autophotosThreadState = ThreadState.SUSPENDED;
        }
        this.autoPhotosDTO = autoPhotosDTO;
    }
}
