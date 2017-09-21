package com.raspberry.camera;

public class OveralStateDTO {
    private Boolean securityEnabled;
    private Boolean databaseEnabled;
    private Boolean databaseConnected;
    private Boolean pendriveEnabled;
    private Boolean pendriveConnected;
    private Boolean hotspotEnabled;


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
