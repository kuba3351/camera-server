package com.raspberry.camera.dto;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu ustawień sieci wi-fi
 */
public class NetworkDTO {

    private String ssid;
    private String password;

    @NotNull
    private Boolean hotspot;

    public Boolean getHotspot() {
        return hotspot;
    }

    public void setHotspot(Boolean hotspot) {
        this.hotspot = hotspot;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
