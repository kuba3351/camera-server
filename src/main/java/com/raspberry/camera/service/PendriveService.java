package com.raspberry.camera.service;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Component
public class PendriveService {
    public boolean checkIfPendriveConnected() {
        return new File("/dev/sda").exists();
    }

    public Optional<String> checkWherePendriveMounted() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("mount");
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().filter(line -> line.startsWith("/dev/sda"))
                .map(line -> new ArrayList<>(Arrays.asList(line.split(" "))))
                .map(list -> list.get(2)).findFirst();
    }

    public boolean mountPendrive() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("sudo mount /dev/sda /home/pi/pendrive");
        return process.waitFor() == 0;
    }
}
