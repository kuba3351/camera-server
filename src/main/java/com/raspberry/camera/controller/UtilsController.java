package com.raspberry.camera.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class UtilsController {

    @GetMapping("/api/whatIsMyIp")
    public void getIp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getOutputStream().write(request.getRemoteAddr().getBytes());
    }

    @GetMapping("/rebootServer")
    public void reboot() throws InterruptedException {
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
}
