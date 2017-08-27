package com.raspberry.camera;

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
    private Blob image;

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

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }
}
