package com.raspberry.camera.service;

import com.raspberry.camera.entity.DatabaseType;
import com.raspberry.camera.entity.TimeThreadState;
import com.raspberry.camera.dto.*;
import org.apache.log4j.Logger;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Created by jakub on 21.08.17.
 */
@Service
public class ConfigFileService {

    private SavingPlacesDTO savingPlacesDTO;
    private TimeDTO timeDTO;
    private UsernameAndPasswordDTO usernameAndPasswordDTO;
    private NetworkDTO networkDTO;

    public PhotoResolutionDTO getPhotoResolutionDTO() {
        return photoResolutionDTO;
    }

    private PhotoResolutionDTO photoResolutionDTO;

    public NetworkDTO getNetworkDTO() {
        return networkDTO;
    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    private Wini file;

    private final static Logger logger = Logger.getLogger(ConfigFileService.class);

    @Autowired
    public ConfigFileService() throws Exception {
        logger.info("Otwieranie pliku konfiguracyjnego...");
        File config = new File("/home/pi/server.ini");
        if (!config.exists())
            config.createNewFile();
        file = new Wini(config);
        logger.info("Wczytywanie konfiguracji czasomierza...");
        timeDTO = readTimeConfig();
        logger.info("Wczytywanie konfiguracji miejsc zapisu...");
        savingPlacesDTO = readSavingPlaces();
        logger.info("Wczytywanie konfiguracji zabezpieczeń...");
        usernameAndPasswordDTO = readUsernameAndPasswordDTOFromFile();
        logger.info("Wczytywanie konfiguracji sieci...");
        networkDTO = readNetworkDTO();
        logger.info("Wczytywanie konfiguracji rozdzielczości...");
        photoResolutionDTO = readPhotoResolution();
        logger.info("Zakończono wczytywanie konfiguracji.");
    }

    public String readRobotIp() {
        return file.get("Robot", "robot.ipAddress");
    }

    public void saveRobotIp(String ip) throws IOException {
        file.put("Robot", "robot.ipAddress", ip);
        file.store();
    }

    public PhotoResolutionDTO readPhotoResolution() {
        PhotoResolutionDTO photoResolutionDTO = new PhotoResolutionDTO();
        Profile.Section photo = file.get("Photo");
        if(photo.containsKey("photo.width"))
            photoResolutionDTO.setWidth(Integer.parseInt(photo.get("photo.width")));
        if(photo.containsKey("photo.height"))
            photoResolutionDTO.setHeigth(Integer.parseInt(photo.get("photo.height")));
        return photoResolutionDTO;
    }

    public void savePhotoResolution(PhotoResolutionDTO photoResolutionDTO) throws IOException {
        file.put("Photo", "photo.width", photoResolutionDTO.getWidth());
        file.put("Photo", "photo.height", photoResolutionDTO.getHeigth());
        file.store();
        this.photoResolutionDTO = photoResolutionDTO;
    }

    private NetworkDTO readNetworkDTO() {
        NetworkDTO networkDTO = new NetworkDTO();
        if(file.containsKey("Network")) {
            Profile.Section network = file.get("Network");
            if(network.containsKey("network.ssid"))
                networkDTO.setSsid(network.get("network.ssid"));
            if(network.containsKey("network.password"))
                networkDTO.setPassword(network.get("network.password"));
            if(network.containsKey("network.hotspot"))
                networkDTO.setHotspot(Boolean.parseBoolean(network.get("network.hotspot")));
        }
        return networkDTO;
    }

    public void writeNetworkDTO(NetworkDTO networkDTO) throws IOException {
        file.put("Network", "network.ssid", networkDTO.getSsid());
        file.put("Network", "network.password", networkDTO.getPassword());
        file.put("Network", "network.hotspot", networkDTO.getHotspot());
        file.store();
        this.networkDTO = networkDTO;
    }

    public TimeDTO getTimeDTO() {
        return timeDTO;
    }

    public SavingPlacesDTO getSavingPlacesDTO() {
        return savingPlacesDTO;
    }

    public Wini getFile() {
        return file;
    }

    public void setFile(Wini file) {
        this.file = file;
    }

    private TimeDTO readTimeConfig() {
        if (file.get("Timer").containsKey("timer.lastValue")) {
            TimeDTO timer = TimeDTO.parseFromString(file.get("Timer", "timer.lastValue"));
            timer.setTimeThreadState(TimeThreadState.NEW);
            return timer;
        }
        return new TimeDTO();
    }

    public void writeTimeToConfigFile(String time) throws IOException {
        file.put("Timer", "timer.lastValue", time);
        file.store();
        timeDTO = TimeDTO.parseFromString(time);
    }

    private DatabaseConfigDTO readDatabaseConfig() throws Exception {
        logger.info("Wczytywanie konfiguracji bazy danych...");
        DatabaseConfigDTO databaseConfigDTO = new DatabaseConfigDTO();
        Profile.Section databaseSection = null;
        if(file.containsKey("Database"))
            databaseSection = file.get("Database");
        else return databaseConfigDTO;
        if (databaseSection.containsKey("database.type")) {
            switch (file.get("Database", "database.type")) {
                case "POSTGRES":
                    databaseConfigDTO.setDatabaseType(DatabaseType.POSTGRES);
                    break;
                case "MYSQL":
                    databaseConfigDTO.setDatabaseType(DatabaseType.MYSQL);
                    break;
                default:
                    throw new Exception("Database type not supported");
            }
        }
        if (databaseSection.containsKey("database.host"))
            databaseConfigDTO.setHost(databaseSection.get("database.host"));
        if (databaseSection.containsKey("database.port"))
            databaseConfigDTO.setPort(Integer.parseInt(databaseSection.get("database.port")));
        if (databaseSection.containsKey("database.name"))
            databaseConfigDTO.setDatabaseName(databaseSection.get("database.name"));
        if (databaseSection.containsKey("database.login"))
            databaseConfigDTO.setUser(databaseSection.get("database.login"));
        if (databaseSection.containsKey("database.password"))
            databaseConfigDTO.setPassword(databaseSection.get("database.password"));
        return databaseConfigDTO;
    }

    private void writeDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) throws IOException {
        file.put("Database", "database.type", databaseConfigDTO.getDatabaseType().toString());
        file.put("Database", "database.host", databaseConfigDTO.getHost());
        file.put("Database", "database.port", databaseConfigDTO.getPort());
        file.put("Database", "database.name", databaseConfigDTO.getDatabaseName());
        file.put("Database", "database.login", databaseConfigDTO.getUser());
        file.put("Database", "database.password", databaseConfigDTO.getPassword());
        file.store();
    }

    private SavingPlacesDTO readSavingPlaces() throws Exception {
        SavingPlacesDTO savingPlacesDTO = new SavingPlacesDTO();
        Profile.Section savingPlaces = null;
        if(file.containsKey("SavingPlaces"))
            savingPlaces = file.get("SavingPlaces");
        else return savingPlacesDTO;
        if (savingPlaces.containsKey("savingPlaces.jpgComputerSave"))
            savingPlacesDTO.setJpgComputerSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgComputerSave")));
        if (savingPlaces.containsKey("savingPlaces.jpgRaspberryPendriveSave"))
            savingPlacesDTO.setJpgRaspberryPendriveSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgRaspberryPendriveSave")));
        if (savingPlaces.containsKey("savingPlaces.jpgDatabaseSave"))
            savingPlacesDTO.setJpgDatabaseSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgDatabaseSave")));
        if (savingPlaces.containsKey("savingPlaces.matPendriveSave"))
            savingPlacesDTO.setMatPendriveSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.matPendriveSave")));
        if (savingPlaces.containsKey("savingPlaces.matDatabaseSave"))
            savingPlacesDTO.setMatDatabaseSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.matDatabaseSave")));
        if(savingPlaces.containsKey("savingPlaces.jpgLocation") && savingPlaces.get("savingPlaces.jpgLocation") != null)
            savingPlacesDTO.setJpgComputerLocation(savingPlaces.get("savingPlaces.jpgLocation"));
        savingPlacesDTO.setDatabaseConfig(readDatabaseConfig());
        return savingPlacesDTO;
    }

    public void writeSavingPlaces(SavingPlacesDTO savingPlacesDTO) throws IOException {
        file.put("SavingPlaces","savingPlaces.jpgComputerSave",savingPlacesDTO.getJpgComputerSave().toString());
        file.put("SavingPlaces","savingPlaces.jpgRaspberryPendriveSave", savingPlacesDTO.getJpgPendriveSave().toString());
        file.put("SavingPlaces","savingPlaces.jpgDatabaseSave",savingPlacesDTO.getJpgDatabaseSave().toString());
        file.put("SavingPlaces","savingPlaces.jpgLocation",savingPlacesDTO.getJpgComputerLocation());
        file.put("SavingPlaces","savingPlaces.matPendriveSave",savingPlacesDTO.getMatPendriveSave().toString());
        file.put("SavingPlaces","savingPlaces.matDatabaseSave",savingPlacesDTO.getMatDatabaseSave().toString());
        writeDatabaseConfigDTO(savingPlacesDTO.getDatabaseConfig());
        file.store();
        this.savingPlacesDTO = savingPlacesDTO;
    }

    public void writeAuthInfo(UsernameAndPasswordDTO usernameAndPasswordDTO) throws IOException {
        file.put("Security", "security.enabled", usernameAndPasswordDTO.getEnabled());
        file.put("Security", "security.username", usernameAndPasswordDTO.getUsername());
        file.put("Security", "security.password", usernameAndPasswordDTO.getPassword());
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
        file.store();
    }

    private UsernameAndPasswordDTO readUsernameAndPasswordDTOFromFile() {
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
        Profile.Section security = null;
        if(file.containsKey("Security")) {
            security = file.get("Security");
        }
        else return usernameAndPasswordDTO;
        if(security.containsKey("security.enabled")) {
            usernameAndPasswordDTO.setEnabled(Boolean.parseBoolean(security.get("security.enabled")));
        }
        if(security.containsKey("security.username")) {
            usernameAndPasswordDTO.setUsername(security.get("security.username"));
        }
        if(security.containsKey("security.password")) {
            usernameAndPasswordDTO.setPassword(security.get("security.password"));
        }
        return usernameAndPasswordDTO;
    }
}
