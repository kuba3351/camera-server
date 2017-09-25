package com.raspberry.camera;

public class NetworkViewDTO {
    private String ssid;
    private String bars;
    private String security;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBars() {
        return bars;
    }

    public void setBars(String bars) {
        this.bars = bars;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }
}
