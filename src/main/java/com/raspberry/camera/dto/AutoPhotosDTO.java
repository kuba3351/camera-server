package com.raspberry.camera.dto;

public class AutoPhotosDTO {
    private Boolean autoPhotosEnabled;
    private Integer autoPhotosDistance;

    public Boolean getAutoPhotosEnabled() {
        return autoPhotosEnabled;
    }

    public void setAutoPhotosEnabled(Boolean autoPhotosEnabled) {
        this.autoPhotosEnabled = autoPhotosEnabled;
    }

    public Integer getAutoPhotosDistance() {
        return autoPhotosDistance;
    }

    public void setAutoPhotosDistance(Integer autoPhotosDistance) {
        this.autoPhotosDistance = autoPhotosDistance;
    }
}
