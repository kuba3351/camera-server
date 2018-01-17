package com.raspberry.camera.service;

import com.raspberry.camera.FakeHttpServletResponse;
import com.raspberry.camera.controller.PhotoController;
import com.raspberry.camera.dto.AutoPhotosDTO;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
public class SensorDistanceListener implements Runnable {

    private final static Logger logger = Logger.getLogger(SensorDistanceListener.class);

    private final RabbitSender rabbitSender;

    private final PhotoController photoController;

    private AutoPhotosDTO autoPhotosDTO;

    @Autowired
    public SensorDistanceListener(RabbitSender rabbitSender, PhotoController photoController, ConfigFileService configFileService) {
        this.rabbitSender = rabbitSender;
        this.photoController = photoController;
        this.autoPhotosDTO = configFileService.readAutoPhotosDTO();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
