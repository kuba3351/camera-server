package com.raspberry.camera.entity;

import javax.persistence.*;
import java.sql.Blob;

@Entity
public class Matrix {

    @Id
    @GeneratedValue
    private long id;

    private int camera;

    private int channel;

    @Lob
    private byte[][] matrix;

    @ManyToOne
    private MatImageEntity image;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public byte[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(byte[][] matrix) {
        this.matrix = matrix;
    }

    public MatImageEntity getImage() {
        return image;
    }

    public void setImage(MatImageEntity image) {
        this.image = image;
    }
}
