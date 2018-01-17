package com.raspberry.camera.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by jakub on 23.08.17.
 */
@Entity
public class MatImage {

    @Id
    @GeneratedValue
    private Long id;
    private int camera;

    @Lob
    private byte[] image;
    private LocalDateTime time;

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
