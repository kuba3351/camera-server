package com.raspberry.camera.dto;

import java.io.File;

public class OveralStateDTO {
    private Boolean securityEnabled;
    private Boolean databaseEnabled;
    private Boolean databaseConnected;
    private Boolean pendriveEnabled;
    private Boolean pendriveConnected;
    private Boolean pendriveMounted;
    private Boolean hotspotEnabled;
    private Boolean jpgComputerSaveEnabled;
    private String jpgLocation;
    private Integer cameras;

    public Boolean getPendriveMounted() {
        return pendriveMounted;
    }

    public void setPendriveMounted(Boolean pendriveMounted) {
        this.pendriveMounted = pendriveMounted;
    }

    public Integer getCameras() {
        return cameras;
    }

    public void setCameras(Integer cameras) {
        this.cameras = cameras;
    }

    public String getJpgLocation() {
        return jpgLocation;
    }

    public void setJpgLocation(String jpgLocation) {
        this.jpgLocation = jpgLocation;
    }

    public Boolean getJpgComputerSaveEnabled() {
        return jpgComputerSaveEnabled;
    }

    public void setJpgComputerSaveEnabled(Boolean jpgComputerSaveEnabled) {
        this.jpgComputerSaveEnabled = jpgComputerSaveEnabled;
    }

    public Boolean getSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(Boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public Boolean getDatabaseEnabled() {
        return databaseEnabled;
    }

    public void setDatabaseEnabled(Boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public Boolean getDatabaseConnected() {
        return databaseConnected;
    }

    public void setDatabaseConnected(Boolean databaseConnected) {
        this.databaseConnected = databaseConnected;
    }

    public Boolean getPendriveEnabled() {
        return pendriveEnabled;
    }

    public void setPendriveEnabled(Boolean pendriveEnabled) {
        this.pendriveEnabled = pendriveEnabled;
    }

    public Boolean getPendriveConnected() {
        return pendriveConnected;
    }

    public void setPendriveConnected(Boolean pendriveConnected) {
        this.pendriveConnected = pendriveConnected;
    }

    public Boolean getHotspotEnabled() {
        return hotspotEnabled;
    }

    public void setHotspotEnabled(Boolean hotspotEnabled) {
        this.hotspotEnabled = hotspotEnabled;
    }

}
