package com.raspberry.camera.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu adresu Ip robota
 */
public class RobotIpDTO {

    @NotNull
    @NotEmpty
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
