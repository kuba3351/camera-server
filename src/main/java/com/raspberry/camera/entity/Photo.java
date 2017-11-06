package com.raspberry.camera.entity;

import org.opencv.core.Mat;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class Photo {
    private Mat matImage;
    private byte[] jpgImage;

    public Photo(Mat matImage, byte[] jpgImage) {
        this.matImage = matImage;
        this.jpgImage = jpgImage;
    }

    public Mat getMatImage() {

        return matImage;
    }

    public byte[] getJpgImage() {
        return jpgImage;
    }
}
