package com.raspberry.camera.service;

import com.raspberry.camera.FakeHttpServletResponse;
import com.raspberry.camera.controller.PhotoController;
import com.raspberry.camera.dto.AutoPhotosDTO;
import com.raspberry.camera.entity.ThreadState;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
        this.autoPhotosDTO = configFileService.readAutoPhotosDTO();
        autophotosThreadState = ThreadState.NEW;
        autoPhotosThread = new Thread(this);
        if (autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.start();
            autophotosThreadState = ThreadState.RUNNING;
        }
    }

    @Override
    public void run() {
        logger.info("Uruchamiam sensor odległości...");
        EV3UltrasonicSensor sensor = new EV3UltrasonicSensor(SensorPort.S1);
        SampleProvider provider = sensor.getDistanceMode();
        float[] sample = new float[provider.sampleSize()];
        while (true) {
            provider.fetchSample(sample, 0);
            if (sample[0] == autoPhotosDTO.getAutoPhotosDistance())
                try {
                    rabbitSender.send("Taking photo...");
                    photoController.takePhoto(new FakeHttpServletResponse());
                    rabbitSender.send("Photo taken!");
                } catch (IOException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    public void setAutoPhotosDTO(AutoPhotosDTO autoPhotosDTO) throws IOException {
        configFileService.writeAutophotosDTO(autoPhotosDTO);
        if(autophotosThreadState.equals(ThreadState.NEW) && autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.start();
        }
        if(autophotosThreadState.equals(ThreadState.SUSPENDED) && autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.resume();
        }
        if(autophotosThreadState.equals(ThreadState.RUNNING) && !autoPhotosDTO.getAutoPhotosEnabled() && RobotService.isRobotConnected()) {
            autoPhotosThread.suspend();
        }
        this.autoPhotosDTO = autoPhotosDTO;
    }

    public void startListener() {
        autoPhotosThread.start();
        autophotosThreadState = ThreadState.RUNNING;
    }
}
