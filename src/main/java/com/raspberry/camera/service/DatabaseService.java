package com.raspberry.camera.service;

import com.raspberry.camera.entity.JpgImageEntity;
import com.raspberry.camera.entity.MatImageEntity;
import com.raspberry.camera.dto.DatabaseConfigDTO;
import com.raspberry.camera.entity.Matrix;
import com.raspberry.camera.entity.Photo;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Created by jakub on 17.08.17.
 */
@Service
public class DatabaseService {
    private Session databaseSession;
    private DatabaseConfigDTO databaseConfigDTO;

    private final static Logger logger = Logger.getLogger(DatabaseService.class);

    public void setDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) {
        this.databaseConfigDTO = databaseConfigDTO;
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
                .addAnnotatedClass(MatImageEntity.class)
                .addAnnotatedClass(Matrix.class);
        switch (databaseConfigDTO.getDatabaseType()) {
            case MYSQL:
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
            break;
            case ORACLE:
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
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
        byte[][][] image1 = new byte[3][mat.rows()][mat.cols()];
        byte[][][] image2 = new byte[3][mat.rows()][mat.cols()];
        for(int i = 0;i<mat.rows();i++) {
            for (int j = 0; j < mat.cols(); j++) {
                byte[] buffer = new byte[3];
                mat.get(i, j, buffer);
                image1[0][i][j] = buffer[0];
                image1[1][i][j] = buffer[1];
                image1[2][i][j] = buffer[2];
            }
        }
        for(int i = 0;i<mat2.rows();i++) {
            for (int j = 0; j < mat2.cols(); j++) {
                byte[] buffer = new byte[3];
                mat2.get(i, j, buffer);
                image2[0][i][j] = buffer[0];
                image2[1][i][j] = buffer[1];
                image2[2][i][j] = buffer[2];
            }
        }
        MatImageEntity matImageEntity = new MatImageEntity();
        matImageEntity.setTime(LocalDateTime.now());
        Transaction transaction = databaseSession.beginTransaction();
        databaseSession.save(matImageEntity);
        transaction.commit();
        for(int i = 0;i<2;i++) {
            byte[][] table = null;
            for(int j = 0;j<3;j++) {
                if(i == 0)
                    table = image1[j];
                if(i == 1)
                    table = image2[j];
                transaction = databaseSession.beginTransaction();
                Matrix matrix = new Matrix();
                matrix.setCamera(i + 1);
                matrix.setChannel(j + 1);
                matrix.setMatrix(table);
                matrix.setImage(matImageEntity);
                logger.info("Zapisuję: obraz:"+(i+1)+" kanał:"+(j+1));
                databaseSession.save(matrix);
                transaction.commit();
            }
        }
        logger.info("Macierz zapisana.");
    }
}
