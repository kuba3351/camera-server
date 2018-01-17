package com.raspberry.camera.service;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

public class TakePhotoCallable implements Runnable {

    private PhotoService photoService;

    public TakePhotoCallable(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Override
    public void run() {
        try {
            photoService.takePhotos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
