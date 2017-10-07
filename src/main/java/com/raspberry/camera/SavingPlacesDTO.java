package com.raspberry.camera;

import java.io.File;

/**
 * Created by jakub on 09.08.17.
 */
public class SavingPlacesDTO {
    private Boolean jpgComputerSave;
    private String jpgComputerLocation;
    private Boolean jpgRaspberryPendriveSave;
    private Boolean jpgDatabaseSave;
    private DatabaseConfigDTO databaseConfig;
    private Boolean matPendriveSave;
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

    public Boolean getJpgRaspberryPendriveSave() {
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
