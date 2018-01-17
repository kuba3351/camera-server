package com.raspberry.camera.service;

import com.raspberry.camera.MatUtils;
import com.raspberry.camera.dto.DatabaseConfigDTO;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.entity.JpgImage;
import com.raspberry.camera.other.MatContainer;
import com.raspberry.camera.entity.MatImage;
import com.raspberry.camera.other.Photo;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.time.LocalDateTime;

/**
 * Serwis służący do zarządzania połączeniem z bazą danych
 */
@Service
public class DatabaseService {

    private final static Logger logger = Logger.getLogger(DatabaseService.class);
    private ConfigFileService configFileService;
    private Session databaseSession;
    private DatabaseConfigDTO databaseConfigDTO;

    @Autowired
    public DatabaseService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        databaseConfigDTO = configFileService.getSavingPlacesDTO().getDatabaseConfig();
        if (configFileService.getSavingPlacesDTO().getMatDatabaseSave() || configFileService.getSavingPlacesDTO().getJpgDatabaseSave()) {
            Thread thread = new Thread(() -> {
                logger.info("Łączenie z bazą danych...");
                try {
                    setUpDatabaseSession(databaseConfigDTO);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("Błąd połączenia z bazą.");
                }
            });
            thread.start();
        }
    }

    public Session getDatabaseSession() {
        return databaseSession;
    }

    public DatabaseConfigDTO getDatabaseConfigDTO() {
        return databaseConfigDTO;
    }

    public void setDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) throws IOException {
        this.databaseConfigDTO = databaseConfigDTO;
        SavingPlacesDTO savingPlacesDTO = configFileService.getSavingPlacesDTO();
        savingPlacesDTO.setDatabaseConfig(databaseConfigDTO);
        configFileService.writeSavingPlaces(savingPlacesDTO);
    }

    public synchronized void setUpDatabaseSession(DatabaseConfigDTO databaseConfigDTO) throws Exception {
        if (databaseSession != null && databaseSession.isOpen()) {
            databaseSession.close();
        }
        logger.info("Łączę z bazą...");
        Configuration configuration = new Configuration()
                .addAnnotatedClass(JpgImage.class)
                .addAnnotatedClass(MatImage.class);
        switch (databaseConfigDTO.getDatabaseType()) {
            case MYSQL:
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                break;
            case POSTGRES:
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                break;
            default:
                throw new Exception("Database type not supported!");
        }
        configuration.setProperty("hibernate.connection.url", databaseConfigDTO.getDatabaseUrl());
        configuration.setProperty("hibernate.connection.username", databaseConfigDTO.getUser());
        configuration.setProperty("hibernate.connection.password", databaseConfigDTO.getPassword());
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        databaseSession = configuration.buildSessionFactory().openSession();
        this.databaseConfigDTO = databaseConfigDTO;
        logger.info("Połączono z bazą.");
    }

    public void saveJpgIntoDatabase(Photo photo1, Photo photo2) throws Exception {
        logger.info("Zapisuję zdjęcie jpg w bazie...");
        if (databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        JpgImage jpgImage = new JpgImage();
        jpgImage.setTime(LocalDateTime.now());
        if (photo1 != null) {
            Blob blob1 = Hibernate.getLobCreator(databaseSession).createBlob(photo1.getJpgImage());
            jpgImage.setCamera1(blob1);
        }
        if (photo2 != null) {
            Blob blob2 = Hibernate.getLobCreator(databaseSession).createBlob(photo2.getJpgImage());
            jpgImage.setCamera2(blob2);
        }
        saveObject(jpgImage);
        logger.info("Zdjęcie zapisane.");
    }

    private synchronized void saveObject(Object object) throws Exception {
        Transaction transaction;
        try {
            transaction = databaseSession.beginTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            databaseSession.getTransaction().rollback();
            throw new Exception();
        }
        try {
            databaseSession.save(object);
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
            throw new Exception("Transaction rolled back!");
        }
        transaction.commit();
    }

    public void saveMatFromMatContainer(MatContainer mat1, MatContainer mat2) throws Exception {
        logger.info("Zapisuję macierz Mat do bazy...");
        if (databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        LocalDateTime now = LocalDateTime.now();
        if(mat1 != null) {
            MatImage matImage = new MatImage();
            matImage.setTime(now);
            matImage.setImage(MatUtils.writeMat(mat1).toByteArray());
            matImage.setCamera(1);
            saveObject(matImage);
            logger.info("Zapisano macierz 1");
        }
        if(mat2 != null) {
            MatImage matImage = new MatImage();
            matImage.setTime(now);
            matImage.setImage(MatUtils.writeMat(mat2).toByteArray());
            matImage.setCamera(2);
            saveObject(matImage);
            logger.info("Zapisano macierz 2");
        }
    }
}