package com.raspberry.camera.service;

import org.springframework.stereotype.Component;

@Component
public class RabbitReceiver {

    public void receive(byte[] str) {
        System.out.println("Received: "+new String(str));
    }

    public void receive(String str) {
        System.out.println("Received: "+str);
    }
}
