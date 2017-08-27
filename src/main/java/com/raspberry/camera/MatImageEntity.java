package com.raspberry.camera;

import org.opencv.core.Mat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.LocalDateTime;

/**
 * Created by jakub on 23.08.17.
 */
@Entity
public class MatImageEntity {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime time;

    @Lob
    private String json;

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

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
