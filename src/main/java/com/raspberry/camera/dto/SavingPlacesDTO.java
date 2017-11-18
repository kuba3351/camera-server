package com.raspberry.camera.dto;

import javax.validation.constraints.NotNull;

/**
 * Created by jakub on 09.08.17.
 */
public class SavingPlacesDTO {

    @NotNull
    private Boolean jpgComputerSave;
    private String jpgComputerLocation;

    @NotNull
    private Boolean jpgRaspberryPendriveSave;

    @NotNull
    private Boolean jpgDatabaseSave;

    @NotNull
    private DatabaseConfigDTO databaseConfig;

    @NotNull
    private Boolean matPendriveSave;

    @NotNull
    private Boolean matDatabaseSave;

    public Boolean getJpgComputerSave() {
        return jpgComputerSave;
    }

    public void setJpgComputerSave(Boolean jpgComputerSave) {
        this.jpgComputerSave = jpgComputerSave;
    }

    public String getJpgComputerLocation() {
        return jpgComputerLocation;
    }

    public void setJpgComputerLocation(String jpgComputerLocation) {
        this.jpgComputerLocation = jpgComputerLocation;
    }

    public Boolean getJpgPendriveSave() {
        return jpgRaspberryPendriveSave;
    }

    public void setJpgRaspberryPendriveSave(Boolean jpgRaspberryPendriveSave) {
        this.jpgRaspberryPendriveSave = jpgRaspberryPendriveSave;
    }

    public Boolean getJpgDatabaseSave() {
        return jpgDatabaseSave;
    }

    public void setJpgDatabaseSave(Boolean jpgDatabaseSave) {
        this.jpgDatabaseSave = jpgDatabaseSave;
    }

    public DatabaseConfigDTO getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfigDTO databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Boolean getMatPendriveSave() {
        return matPendriveSave;
    }

    public void setMatPendriveSave(Boolean matPendriveSave) {
        this.matPendriveSave = matPendriveSave;
    }

    public Boolean getMatDatabaseSave() {
        return matDatabaseSave;
    }

    public void setMatDatabaseSave(Boolean matDatabaseSave) {
        this.matDatabaseSave = matDatabaseSave;
    }
}
