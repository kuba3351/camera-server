package com.raspberry.camera.controller;

import com.raspberry.camera.dto.PhotoResolutionDTO;
import com.raspberry.camera.service.PhotoResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

/**
 * Kontroler odpowiedzialny za ustawienia rozdzielczości zdjęć
 */
@RestController
public class PhotoResolutionController {

    private PhotoResolutionService photoResolutionService;

    @Autowired
    public PhotoResolutionController(PhotoResolutionService photoResolutionService) {
        this.photoResolutionService = photoResolutionService;
    }

    @GetMapping("/api/photo/resolution")
    public PhotoResolutionDTO getPhotoResolution() {
        return photoResolutionService.getPhotoResolutionDTO();
    }

    @PostMapping("/api/photo/resolution")
    public ResponseEntity setPhotoResolution(@RequestBody @Valid PhotoResolutionDTO photoResolutionDTO) {
        try {
            photoResolutionService.setPhotoResolutionDTO(photoResolutionDTO);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
