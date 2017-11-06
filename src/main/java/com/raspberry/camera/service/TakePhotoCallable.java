package com.raspberry.camera.service;

import com.raspberry.camera.entity.Photo;

import java.util.concurrent.Callable;

public class TakePhotoCallable implements Callable<Photo> {

    private int camera;
    private PhotoService photoService;

    public TakePhotoCallable(int camera, PhotoService photoService) {
        this.camera = camera;
        this.photoService = photoService;
    }

    @Override
    public Photo call() throws Exception {
        return photoService.takePhoto(camera);
    }
}
