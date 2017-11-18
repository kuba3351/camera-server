package com.raspberry.camera.service;

import com.raspberry.camera.MatUtils;
import com.raspberry.camera.dto.SavingPlacesDTO;
import com.raspberry.camera.entity.JpgImageEntity;
import com.raspberry.camera.entity.MatEntity;
import com.raspberry.camera.entity.MatImageEntity;
import com.raspberry.camera.dto.DatabaseConfigDTO;
import com.raspberry.camera.entity.Photo;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.time.LocalDateTime;

/**
 * Created by jakub on 17.08.17.
 */
@Service
public class DatabaseService {

    private ConfigFileService configFileService;

    @Autowired
    public DatabaseService(ConfigFileService configFileService) {
        this.configFileService = configFileService;
        databaseConfigDTO = configFileService.getSavingPlacesDTO().getDatabaseConfig();
        if(configFileService.getSavingPlacesDTO().getMatDatabaseSave() || configFileService.getSavingPlacesDTO().getJpgDatabaseSave()) {
            logger.info("Łączenie z bazą danych...");
            try {
                setUpDatabaseSession(databaseConfigDTO);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("Błąd połączenia z bazą.");
            }
        }
    }

    private Session databaseSession;
    private DatabaseConfigDTO databaseConfigDTO;

    private final static Logger logger = Logger.getLogger(DatabaseService.class);

    public void setDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) throws IOException {
        this.databaseConfigDTO = databaseConfigDTO;
        SavingPlacesDTO savingPlacesDTO = configFileService.getSavingPlacesDTO();
        savingPlacesDTO.setDatabaseConfig(databaseConfigDTO);
        configFileService.writeSavingPlaces(savingPlacesDTO);
    }

    public Session getDatabaseSession() {
        return databaseSession;
    }

    public DatabaseConfigDTO getDatabaseConfigDTO() {
        return databaseConfigDTO;
    }

    public synchronized void setUpDatabaseSession(DatabaseConfigDTO databaseConfigDTO) throws Exception {
        if(databaseSession != null && databaseSession.isOpen()) {
            databaseSession.close();
        }
        logger.info("Łączę z bazą...");
        Configuration configuration = new Configuration()
                .addAnnotatedClass(JpgImageEntity.class)
                .addAnnotatedClass(MatImageEntity.class);
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
        if(databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        JpgImageEntity jpgImageEntity = new JpgImageEntity();
        jpgImageEntity.setTime(LocalDateTime.now());
        if(photo1 != null) {
            Blob blob1 = Hibernate.getLobCreator(databaseSession).createBlob(photo1.getJpgImage());
            jpgImageEntity.setCamera1(blob1);
        }
        if(photo2 != null) {
            Blob blob2 = Hibernate.getLobCreator(databaseSession).createBlob(photo2.getJpgImage());
            jpgImageEntity.setCamera2(blob2);
        }
        Transaction transaction = databaseSession.beginTransaction();
        databaseSession.save(jpgImageEntity);
        transaction.commit();
        logger.info("Zdjęcie zapisane.");
    }

    public void saveMatToDatabase(Mat mat, Mat mat2) throws Exception {
        logger.info("Zapisuję macierz Mat do bazy...");
        if(databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        Transaction transaction = databaseSession.getTransaction();
        LocalDateTime now = LocalDateTime.now();
        if(mat != null) {
            int[] tab1 = MatUtils.extractDataFromMat(mat);
            MatEntity matEntity1 = new MatEntity();
            matEntity1.setCols(mat.cols());
            matEntity1.setRows(mat.rows());
            matEntity1.setData(tab1);
            ByteArrayOutputStream outputStream = MatUtils.writeMat(matEntity1);
            MatImageEntity matImageEntity = new MatImageEntity();
            matImageEntity.setTime(now);
            matImageEntity.setImage(outputStream.toByteArray());
            matImageEntity.setCamera(1);
            transaction = databaseSession.beginTransaction();
            databaseSession.save(matImageEntity);
            logger.info("Zapisano macierz 1");
        }
        if(mat2 != null) {
            int[] tab2 = MatUtils.extractDataFromMat(mat2);
            MatEntity matEntity2 = new MatEntity();
            matEntity2.setRows(mat2.rows());
            matEntity2.setCols(mat2.cols());
            matEntity2.setData(tab2);
            ByteArrayOutputStream outputStream2 = MatUtils.writeMat(matEntity2);
            MatImageEntity matImageEntity2 = new MatImageEntity();
            matImageEntity2.setTime(now);
            matImageEntity2.setImage(outputStream2.toByteArray());
            matImageEntity2.setCamera(2);
            logger.info("Zapisano macierz 2");
        }
        transaction.commit();
    }
}
