package com.raspberry.camera.controller;

import com.raspberry.camera.dto.TimeDTO;
import com.raspberry.camera.entity.ThreadState;
import com.raspberry.camera.service.TimerService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

/**
 * Kontroler odpowiedzialny za ustawienia czasomierza
 */

@RestController
public class TimeController {

    private final static Logger logger = Logger.getLogger(TimeController.class);

    private Thread timeThread;
    private TimerService timerService;

    private TimeDTO timer;

    @Autowired
    public TimeController(TimerService timerService) throws IOException {
        timeThread = new Thread(timerService);
        this.timerService = timerService;
    }

    @GetMapping("/api/time")
    public TimeDTO getTimeInfo() {
        return timerService.getTimer();
    }

    @PostMapping("/api/time")
    public void setTime(@RequestBody @Valid TimeDTO timeDTO) {
        logger.info("Odebrano żądanie ustawienia czasomierza. ");
        Thread thread = new Thread(() -> {
            try {
                timeDTO.reset();
                timerService.setTimer(timeDTO);
                logger.info("Przetworzono żądanie ustawienia czasomierza. Czasomierz ustawiony na: " + timeDTO.toString());
                timer.setThreadState(ThreadState.NEW);
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
        timerService.getTimer().setThreadState(ThreadState.RUNNING);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/time/stop")
    public ResponseEntity stop() throws Exception {
        logger.info("Odebrano żądanie wtrzymania czasomierza.");
        if (timerService.getTimer().getThreadState().equals(ThreadState.RUNNING))
            timeThread.suspend();
        logger.info("Przetworzono żądanie wstrzymania czasomierza.");
        timerService.getTimer().setThreadState(ThreadState.SUSPENDED);
        return new ResponseEntity(HttpStatus.OK);
    }

}
