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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Kontroler odpowiedzialny za żądania nie pasujące do innych kontrolerów
 */
@RestController
public class UtilsController {

    private final static Logger logger = Logger.getLogger(UtilsController.class);
    private final PendriveService pendriveService;

    @Autowired
    public UtilsController(PendriveService pendriveService) {
        this.pendriveService = pendriveService;
    }

    @GetMapping("/api/whatIsMyIp")
    public void getIp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getOutputStream().write(request.getRemoteAddr().getBytes());
    }

    @GetMapping("/rebootServer")
    public void reboot() {
        logger.info("RESTART SERWERA ZA 5 SEKUND!!!");
        Thread thread = new Thread(() -> {
            try {
                Runtime runtime = Runtime.getRuntime();
                logger.info("Sprawdzanie usługi camera-server...");
                Process exec = runtime.exec("systemctl status camera-server");
                exec.waitFor();
                InputStream inputStream = exec.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                bufferedReader.lines().filter(line -> line.contains("running")).findFirst().ifPresent((value) -> {
                    try {
                        logger.info("Usługa uruchomiona. Rsetartuję za pomocą systemctl...");
                        runtime.exec("sudo systemctl restart camera-server").waitFor();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                });
                bufferedReader.lines().filter(line -> line.contains("dead")).findFirst().ifPresent((value) -> {
                    logger.info("Usługa nieaktywna. Restartuję za pomocą skryptu...");
                    try {
                        runtime.exec("exec /home/pi/restartServer.sh");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.start();
    }

    @GetMapping("/api/mountPendrive")
    public ResponseEntity mountPendrive() {
        if (!pendriveService.checkIfPendriveConnected()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        try {
            if (pendriveService.checkWherePendriveMounted().isPresent()) {
                return new ResponseEntity(HttpStatus.OK);
            }
            return pendriveService.mountPendrive() ?
                    new ResponseEntity(HttpStatus.OK) :
                    new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
