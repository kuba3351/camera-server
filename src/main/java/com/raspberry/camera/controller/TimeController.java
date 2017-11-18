package com.raspberry.camera.controller;

import com.raspberry.camera.entity.TimeThreadState;
import com.raspberry.camera.service.TimerService;
import com.raspberry.camera.dto.TimeDTO;
import com.raspberry.camera.service.PhotoService;
import com.raspberry.camera.service.RabbitSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final static Logger logger = Logger.getLogger(TimeController.class);

    private Thread timeThread;
    private TimerService timerService;

    private RabbitSender rabbitSender;
    private TimeDTO timer;
    private PhotoController photoController;

    @Autowired
    public TimeController(PhotoService photoService, RabbitSender rabbitSender, PhotoController photoController, TimerService timerService) throws IOException {
        this.photoService = photoService;
        this.rabbitSender = rabbitSender;
        this.photoController = photoController;
        timeThread = new Thread(timerService);
        this.timerService = timerService;
    }

    @GetMapping("/api/time")
    public TimeDTO getTimeInfo() {
        return timerService.getTimer();
    }

    @PostMapping("/api/time")
    public void setTime(@RequestBody TimeDTO timeDTO) {
        logger.info("Odebrano żądanie ustawienia czasomierza. ");
        Thread thread = new Thread(() -> {
            try {
                timeDTO.reset();
                timerService.setTimer(timeDTO);
                logger.info("Przetworzono żądanie ustawienia czasomierza. Czasomierz ustawiony na: "+timeDTO.toString());
                timer.setTimeThreadState(TimeThreadState.NEW);
                this.timer = timeDTO;
                timeThread = new Thread(timerService);
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
        timerService.getTimer().setTimeThreadState(TimeThreadState.RUNNING);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/time/stop")
    public ResponseEntity stop() throws Exception {
        logger.info("Odebrano żądanie wtrzymania czasomierza.");
        if(timerService.getTimer().getTimeThreadState().equals(TimeThreadState.RUNNING))
            timeThread.suspend();
        logger.info("Przetworzono żądanie wstrzymania czasomierza.");
        timerService.getTimer().setTimeThreadState(TimeThreadState.SUSPENDED);
        return new ResponseEntity(HttpStatus.OK);
    }

}
