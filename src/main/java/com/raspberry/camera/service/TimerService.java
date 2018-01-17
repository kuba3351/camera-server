package com.raspberry.camera.service;

import com.raspberry.camera.FakeHttpServletResponse;
import com.raspberry.camera.controller.PhotoController;
import com.raspberry.camera.dto.TimeDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Serwis służący do obsługi czasomierza
 */
@Service
public class TimerService implements Runnable {
    private final static Logger logger = Logger.getLogger(TimerService.class);
    private TimeDTO timer;
    private RabbitSender rabbitSender;
    private ConfigFileService configFileService;
    private PhotoController photoController;

    @Autowired
    public TimerService(RabbitSender rabbitSender, ConfigFileService configFileService, PhotoController photoController) {
        this.timer = configFileService.getTimeDTO();
        this.rabbitSender = rabbitSender;
        this.configFileService = configFileService;
        this.photoController = photoController;
    }

    public TimeDTO getTimer() {
        return timer;
    }

    public void setTimer(TimeDTO timer) throws IOException {
        this.timer = timer;
        configFileService.writeTimeToConfigFile(timer.toString());
    }

    public void run() {
        while (true) {
            logger.info("Rozpoczynam odliczanie. Czasomierz ustawiony na: " + timer.getHours() + ":" + timer.getMinutes() + ":" + timer.getSeconds());
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (timer.tick());
            logger.info("Odliczanie zakończone. Wysyłam info i robię zdjęcie...");
            rabbitSender.send("Taking photo...");
            timer.reset();
            Thread thread = new Thread(() -> {
                try {
                    photoController.takePhoto(new FakeHttpServletResponse());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("Zrobiono zdjęcie. Wysyłam info");
                rabbitSender.send("Photo taken!");
            });
            thread.start();
        }
    }
}
