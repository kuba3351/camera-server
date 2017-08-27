package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by jakub on 03.08.17.
 */

@RestController
public class TimeController {

    private PhotoService photoService;

    private ConfigFileService configFileService;

    private final static Logger logger = Logger.getLogger(TimeController.class);

    private Thread timeThread;

    private RabbitSender rabbitSender;
    private TimeDTO timer;

    public TimeController(PhotoService photoService, ConfigFileService configFileService, RabbitSender rabbitSender) throws IOException {
        this.photoService = photoService;
        this.configFileService = configFileService;
        this.rabbitSender = rabbitSender;
        timer = configFileService.getTimeDTO();
        timeThread = new Thread(new TimerThread(timer, rabbitSender, photoService, configFileService));
    }

    @GetMapping("/api/time")
    public TimeDTO getTimeInfo() {
        return configFileService.getTimeDTO();
    }

    @PostMapping("/api/time")
    public void setTime(@RequestBody TimeDTO timeDTO) {
        logger.info("Odebrano żądanie ustawienia czasomierza. ");
        Thread thread = new Thread(() -> {
            try {
                timeDTO.reset();
                configFileService.writeTimeToConfigFile(timeDTO.toString());
                logger.info("Przetworzono żądanie ustawienia czasomierza. Czasomierz ustawiony na: "+configFileService.getTimeDTO().toString());
                configFileService.getTimeDTO().setTimeThreadState(TimeThreadState.NEW);
                timer = configFileService.getTimeDTO();
                timeThread = new Thread(new TimerThread(timer, rabbitSender, photoService, configFileService));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @GetMapping("/api/time/start")
    public ResponseEntity start() throws Exception {
        logger.info("Odebrano żądanie uruchomienia czasomierza.");
        if (timeThread.getState().equals(Thread.State.NEW))
            timeThread.start();
        else
            timeThread.resume();
        logger.info("Przetworzono żądanie uruchomienia czasomierza.");
        configFileService.getTimeDTO().setTimeThreadState(TimeThreadState.RUNNING);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/time/stop")
    public ResponseEntity stop() throws Exception {
        logger.info("Odebrano żądanie wtrzymania czasomierza.");
        if(configFileService.getTimeDTO().getTimeThreadState().equals(TimeThreadState.RUNNING))
            timeThread.suspend();
        logger.info("Przetworzono żądanie wstrzymania czasomierza.");
        configFileService.getTimeDTO().setTimeThreadState(TimeThreadState.SUSPENDED);
        return new ResponseEntity(HttpStatus.OK);
    }

}
