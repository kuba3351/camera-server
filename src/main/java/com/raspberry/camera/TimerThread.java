package com.raspberry.camera;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;

/**
 * Created by jakub on 23.08.17.
 */
class TimerThread implements Runnable {
    private final TimeDTO timer;
    private final RabbitSender rabbitSender;
    private final PhotoService photoService;
    private final ConfigFileService configFileService;

    private final static Logger logger = Logger.getLogger(TimerThread.class);


    public TimerThread(TimeDTO timer, RabbitSender rabbitSender, PhotoService photoService, ConfigFileService configFileService) {
        this.timer = timer;
        this.rabbitSender = rabbitSender;
        this.photoService = photoService;
        this.configFileService = configFileService;
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
            logger.info("Odliczanie zakończone. Wysyłam event i robię zdjęcie...");
            rabbitSender.send("Taking photo...");
            try {
                photoService.takePhoto(configFileService.getSavingPlacesDTO());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            logger.info("Zrobiono zdjęcie. Wysyłam event i resetuję czasomierz.");
            rabbitSender.send("Photo taken!");
            timer.reset();
        }
    }
}
