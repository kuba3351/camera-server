package com.raspberry.camera;

import com.google.gson.Gson;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;
import java.time.LocalDateTime;

/**
 * Created by jakub on 17.08.17.
 */
@Service
public class DatabaseService {
    private Session databaseSession;
    private DatabaseConfigDTO databaseConfigDTO;

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
        Configuration configuration = new Configuration()
                .addAnnotatedClass(JpgImageEntity.class)
                .addAnnotatedClass(MatImageEntity.class);
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
    }

    public void saveJpgIntoDatabase(File file) throws Exception {
        if(databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        JpgImageEntity jpgImageEntity = new JpgImageEntity();
        jpgImageEntity.setTime(LocalDateTime.now());
        InputStream imageStream = null;
        try {
            imageStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Blob image = Hibernate.getLobCreator(databaseSession).createBlob(imageStream, file.length());
        jpgImageEntity.setImage(image);
        Transaction transaction = databaseSession.beginTransaction();
        databaseSession.save(jpgImageEntity);
        transaction.commit();
    }

    public void saveMatToDatabase(Mat mat) throws Exception {
        if(databaseSession == null || !databaseSession.isOpen()) {
            setUpDatabaseSession(databaseConfigDTO);
        }
        MatImageEntity matImageEntity = new MatImageEntity();
        matImageEntity.setTime(LocalDateTime.now());
        matImageEntity.setJson(new Gson().toJson(mat));
        Transaction transaction = databaseSession.beginTransaction();
        databaseSession.save(matImageEntity);
        transaction.commit();
    }
}
