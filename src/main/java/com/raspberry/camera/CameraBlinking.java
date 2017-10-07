package com.raspberry.camera;

import org.opencv.photo.Photo;

class CameraBlinking implements Runnable {
    private boolean loadingInProgress;

    public CameraBlinking() {
        loadingInProgress = true;
    }

    public void finishBlibking() {
        loadingInProgress = false;
    }

    @Override
    public void run() {
        while (loadingInProgress) {
            PhotoService.takeFirstPhoto();
            PhotoService.release();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        PhotoService.takeFirstPhoto();
    }
}
