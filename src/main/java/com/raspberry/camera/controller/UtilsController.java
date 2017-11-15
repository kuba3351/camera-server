package com.raspberry.camera.controller;

import com.raspberry.camera.service.PendriveService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class UtilsController {

    @Autowired
    private PendriveService pendriveService;

    private final static Logger logger = Logger.getLogger(UtilsController.class);

    @GetMapping("/api/whatIsMyIp")
    public void getIp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getOutputStream().write(request.getRemoteAddr().getBytes());
    }

    @GetMapping("/rebootServer")
    public void reboot() throws InterruptedException {
        logger.info("RESTART SERWERA ZA 5 SEKUND!!!");
        Thread thread = new Thread(() -> {
            try {
                Runtime.getRuntime().exec("sudo systemctl restart camera-server").waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(5000);
        thread.start();
    }

    @GetMapping("/api/mountPendrive")
    public ResponseEntity mountPendrive() throws IOException, InterruptedException {
        if (!pendriveService.checkIfPendriveConnected()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if(pendriveService.checkWherePendriveMounted().isPresent()) {
            return new ResponseEntity(HttpStatus.OK);
        }
        return pendriveService.mountPendrive() ?
                new ResponseEntity(HttpStatus.OK) :
                new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
