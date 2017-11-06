package com.raspberry.camera.service;

import com.raspberry.camera.controller.PhotoController;
import com.raspberry.camera.dto.TimeDTO;
import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by jakub on 23.08.17.
 */
public class TimerThread implements Runnable {
    private final TimeDTO timer;
    private final RabbitSender rabbitSender;
    private final PhotoService photoService;
    private final ConfigFileService configFileService;
    private final PhotoController photoController;

    private final static Logger logger = Logger.getLogger(TimerThread.class);


    public TimerThread(TimeDTO timer, RabbitSender rabbitSender, PhotoService photoService, ConfigFileService configFileService, PhotoController photoController) {
        this.timer = timer;
        this.rabbitSender = rabbitSender;
        this.photoService = photoService;
        this.configFileService = configFileService;
        this.photoController = photoController;
    }

    public void run() {
        while (true) {
            logger.info("Rozpoczynam odliczanie. Czasomierz ustawiony na: " + timer.getHours() + ":" + timer.getMinutes() + ":" + timer.getSeconds());
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (timer.tick());
            logger.info("Odliczanie zakończone. Wysyłam event i robię zdjęcie...");
            rabbitSender.send("Taking photo...");
            try {
                photoController.takePhoto(new HttpServletResponse() {
                    @Override
                    public void addCookie(Cookie cookie) {

                    }

                    @Override
                    public boolean containsHeader(String s) {
                        return false;
                    }

                    @Override
                    public String encodeURL(String s) {
                        return null;
                    }

                    @Override
                    public String encodeRedirectURL(String s) {
                        return null;
                    }

                    @Override
                    public String encodeUrl(String s) {
                        return null;
                    }

                    @Override
                    public String encodeRedirectUrl(String s) {
                        return null;
                    }

                    @Override
                    public void sendError(int i, String s) throws IOException {

                    }

                    @Override
                    public void sendError(int i) throws IOException {

                    }

                    @Override
                    public void sendRedirect(String s) throws IOException {

                    }

                    @Override
                    public void setDateHeader(String s, long l) {

                    }

                    @Override
                    public void addDateHeader(String s, long l) {

                    }

                    @Override
                    public void setHeader(String s, String s1) {

                    }

                    @Override
                    public void addHeader(String s, String s1) {

                    }

                    @Override
                    public void setIntHeader(String s, int i) {

                    }

                    @Override
                    public void addIntHeader(String s, int i) {

                    }

                    @Override
                    public void setStatus(int i) {

                    }

                    @Override
                    public void setStatus(int i, String s) {

                    }

                    @Override
                    public int getStatus() {
                        return 0;
                    }

                    @Override
                    public String getHeader(String s) {
                        return null;
                    }

                    @Override
                    public Collection<String> getHeaders(String s) {
                        return null;
                    }

                    @Override
                    public Collection<String> getHeaderNames() {
                        return null;
                    }

                    @Override
                    public String getCharacterEncoding() {
                        return null;
                    }

                    @Override
                    public String getContentType() {
                        return null;
                    }

                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        return null;
                    }

                    @Override
                    public PrintWriter getWriter() throws IOException {
                        return null;
                    }

                    @Override
                    public void setCharacterEncoding(String s) {

                    }

                    @Override
                    public void setContentLength(int i) {

                    }

                    @Override
                    public void setContentLengthLong(long l) {

                    }

                    @Override
                    public void setContentType(String s) {

                    }

                    @Override
                    public void setBufferSize(int i) {

                    }

                    @Override
                    public int getBufferSize() {
                        return 0;
                    }

                    @Override
                    public void flushBuffer() throws IOException {

                    }

                    @Override
                    public void resetBuffer() {

                    }

                    @Override
                    public boolean isCommitted() {
                        return false;
                    }

                    @Override
                    public void reset() {

                    }

                    @Override
                    public void setLocale(Locale locale) {

                    }

                    @Override
                    public Locale getLocale() {
                        return null;
                    }
                });
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            logger.info("Zrobiono zdjęcie. Wysyłam event i resetuję czasomierz.");
            rabbitSender.send("Photo taken!");
            timer.reset();
        }
    }
}
