package com.raspberry.camera.service;

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
        int[] tab1 = new int[(int)mat.total() * mat.channels()];
        copyTab(mat, tab1);
        int[] tab2 = new int[(int)mat2.total() * mat2.channels()];
        copyTab(mat2, tab2);
        MatEntity matEntity1 = new MatEntity();
        matEntity1.setCols(mat.cols());
        matEntity1.setRows(mat.rows());
        matEntity1.setData(tab1);
        ByteArrayOutputStream outputStream = writeMat(matEntity1);
        MatEntity matEntity2 = new MatEntity();
        matEntity2.setRows(mat2.rows());
        matEntity2.setCols(mat2.cols());
        matEntity2.setData(tab2);
        ByteArrayOutputStream outputStream2 = writeMat(matEntity2);
        MatImageEntity matImageEntity = new MatImageEntity();
        LocalDateTime now = LocalDateTime.now();
        matImageEntity.setTime(now);
        matImageEntity.setImage(outputStream.toByteArray());
        Transaction transaction = databaseSession.beginTransaction();
        databaseSession.save(matImageEntity);
        logger.info("Zapisano macierz 1");
        MatImageEntity matImageEntity2 = new MatImageEntity();
        matImageEntity2.setTime(now);
        matImageEntity2.setImage(outputStream2.toByteArray());
        transaction.commit();
        logger.info("Macierz 2 zapisana.");
    }

    private void copyTab(Mat mat, int[] tab1) {
        int z = 0;
        for(int i = 0;i<mat.rows();i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] temp = mat.get(i, j);
                for(int k = 0;k<temp.length;k++) {
                    tab1[z++] = (int)temp[k];
                }
            }
        }
    }

    private ByteArrayOutputStream writeMat(MatEntity matEntity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder builder = new StringBuilder();
        builder.append("%");
        builder.append("YAML:1.0\n" +
                "---\n" +
                "Matrix: !!opencv-matrix\n" +
                "   rows: "+matEntity.getRows()+"\n" +
                "   cols: "+matEntity.getCols()+"\n" +
                "   dt: \"3u\"\n" +
                "   data: [ ");
        int[] data = matEntity.getData();
        for(int i = 0; i<data.length; i++) {
            builder.append(data[i]);
            if(i != data.length - 1)
                builder.append(", ");
            if(i % 10 == 0 && i>5)
                builder.append("\n      ");
        }
        builder.append("]");
        outputStream.write(builder.toString().getBytes());
        return outputStream;
    }
}
