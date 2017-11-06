package com.raspberry.camera.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.sql.Blob;
import java.time.LocalDateTime;

/**
 * Created by jakub on 17.08.17.
 */
@Entity
public class JpgImageEntity {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime time;

    @Lob
    private Blob camera1;

    @Lob
    private Blob camera2;

    public Blob getCamera2() {
        return camera2;
    }

    public void setCamera2(Blob camera2) {
        this.camera2 = camera2;
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

    public Blob getCamera1() {
        return camera1;
    }

    public void setCamera1(Blob camera1) {
        this.camera1 = camera1;
    }
}
