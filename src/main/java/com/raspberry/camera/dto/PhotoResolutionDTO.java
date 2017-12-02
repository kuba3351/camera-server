package com.raspberry.camera.dto;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu ustawień rozdzielczości zdjęcia
 */
public class PhotoResolutionDTO {

    @NotNull
    private int width;

    @NotNull
    private int heigth;

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
