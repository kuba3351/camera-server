package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by jakub on 29.07.17.
 */

@Controller
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private ConfigFileService configFileService;

    private final static Logger logger = Logger.getLogger(PhotoController.class);

    private static final int BUFFER_SIZE = 1000;

    @GetMapping("/api/takePhoto")
    public void takePhoto(HttpServletResponse httpServletResponse) throws IOException {
        logger.info("odebrano żądanie wykonania zdjęcia...");
        File photo = photoService.takePhoto(configFileService.getSavingPlacesDTO());
        streamPhotoOverHttp(httpServletResponse, photo);
        logger.info("Przetworzono żądanie wykonania zdjęcia.");
    }

    private void streamPhotoOverHttp(HttpServletResponse httpServletResponse, File photo) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(photo);
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        int numOfBytes;
        do {
            byte[] bytes = new byte[BUFFER_SIZE];
            numOfBytes = fileInputStream.read(bytes);
            servletOutputStream.write(bytes);
        }while(numOfBytes == BUFFER_SIZE);
    }

    @GetMapping("/api/getLastPhoto")
    public void getLastPhoto(HttpServletResponse httpServletResponse) throws IOException {
        logger.info("Odebrano zapytanie o ostatnie zdjęcie...");
        streamPhotoOverHttp(httpServletResponse, new File(PhotoService.FILE));
        logger.info("Przetworzono zapytanie o ostatnie zdjęcie...");
    }
}
