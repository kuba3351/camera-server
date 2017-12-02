package com.raspberry.camera.entity;

import org.opencv.core.Mat;

public class Photo {
    private Mat matImage;
    private byte[] jpgImage;

    public Photo(Mat matImage) {
        this.matImage = matImage;
    }

    public Mat getMatImage() {

        return matImage;
    }

    public byte[] getJpgImage() {
        return jpgImage;
    }

    public void setJpgImage(byte[] jpgImage) {
        this.jpgImage = jpgImage;
    }
}
