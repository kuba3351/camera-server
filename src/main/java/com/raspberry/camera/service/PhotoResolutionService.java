package com.raspberry.camera.service;

import com.raspberry.camera.dto.PhotoResolutionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Serwis służący do zarządzania ustawieniami rozdzielczości zdjęcia
 */
@Service
public class PhotoResolutionService {

    private PhotoResolutionDTO photoResolutionDTO;
    private ConfigFileService configFileService;

    @Autowired
    public PhotoResolutionService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        this.photoResolutionDTO = configFileService.getPhotoResolutionDTO();
    }

    public PhotoResolutionDTO getPhotoResolutionDTO() {
        return photoResolutionDTO;
    }

    public void setPhotoResolutionDTO(PhotoResolutionDTO photoResolutionDTO) throws IOException {
        this.photoResolutionDTO = photoResolutionDTO;
        configFileService.writePhotoResolution(photoResolutionDTO);
    }

}
