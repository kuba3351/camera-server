package com.raspberry.camera.dto;

/**
 * Klasa służąca do transferu adresu Ip robota
 */
public class RobotDTO {

    private String ip;
    private String left;
    private String right;
    private String distanceSensor;
    private Boolean connect;
    private Boolean shouldStopOnPhotos;

    public Boolean getShouldStopOnPhotos() {
        return shouldStopOnPhotos;
    }

    public void setShouldStopOnPhotos(Boolean shouldStopOnPhotos) {
        this.shouldStopOnPhotos = shouldStopOnPhotos;
    }

    public Boolean getConnect() {
        return connect;
    }

    public void setConnect(Boolean connect) {
        this.connect = connect;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getDistanceSensor() {
        return distanceSensor;
    }

    public void setDistanceSensor(String distanceSensor) {
        this.distanceSensor = distanceSensor;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
