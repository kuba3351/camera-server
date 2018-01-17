package com.raspberry.camera.dto;

import com.raspberry.camera.other.CameraType;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu ustawień rozdzielczości zdjęcia
 */
public class PhotoDTO {

    @NotNull
    private int width;

    @NotNull
    private int heigth;

    @NotNull
    private CameraType cameraType;

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeigth() {
        return heigth;
    }

    public void setHeigth(int heigth) {
        this.heigth = heigth;
    }
}
