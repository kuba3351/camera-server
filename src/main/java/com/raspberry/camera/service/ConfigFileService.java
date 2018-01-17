package com.raspberry.camera.service;

import com.raspberry.camera.dto.*;
import com.raspberry.camera.other.CameraType;
import com.raspberry.camera.other.DatabaseType;
import com.raspberry.camera.other.ThreadState;
import org.apache.log4j.Logger;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Serwis służący do zarządzania plikiem INI przechowującym konfigurację całego systemu
 */
@Service
public class ConfigFileService {

    private final static Logger logger = Logger.getLogger(ConfigFileService.class);
    private SavingPlacesDTO savingPlacesDTO;
    private TimeDTO timeDTO;
    private UsernameAndPasswordDTO usernameAndPasswordDTO;
    private NetworkDTO networkDTO;
    private PhotoDTO photoDTO;

    public RobotDTO getRobotDTO() {
        return robotDTO;
    }

    public void setRobotDTO(RobotDTO robotDTO) {
        this.robotDTO = robotDTO;
    }

    private RobotDTO robotDTO;

    public AutoPhotosDTO getAutoPhotosDTO() {
        return autoPhotosDTO;
    }

    private AutoPhotosDTO autoPhotosDTO;
    private Wini file;

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
        photoDTO = readPhotoDTO();
        logger.info("Wczytywanie konfiguracji automatycznych zdjęć...");
        autoPhotosDTO = readAutoPhotosDTO();
        logger.info("Wczytywanie ustawień robota...");
        robotDTO = readRobotDTO();
        logger.info("Zakończono wczytywanie konfiguracji.");
    }

    public PhotoDTO getPhotoDTO() {
        return photoDTO;
    }

    public NetworkDTO getNetworkDTO() {
        return networkDTO;
    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public String readRobotIp() {
        return file.get("Robot", "robot.ipAddress");
    }

    public void writeRobotIP(String ip) throws IOException {
        file.put("Robot", "robot.ipAddress", ip);
        file.store();
    }

    public AutoPhotosDTO readAutoPhotosDTO() {
        Profile.Section autoPhotos = file.get("AutoPhotos");
        AutoPhotosDTO autoPhotosDTO = new AutoPhotosDTO();
        if(autoPhotos.containsKey("autophotos.enabled")) {
            autoPhotosDTO.setAutoPhotosEnabled(Boolean.parseBoolean(autoPhotos.get("autophotos.enabled")));
        }
        if(autoPhotos.containsKey("autophotos.distance")) {
            autoPhotosDTO.setAutoPhotosDistance(Integer.parseInt(autoPhotos.get("autophotos.distance")));
        }
        return autoPhotosDTO;
    }

    public RobotDTO readRobotDTO() {
        RobotDTO robotDTO = new RobotDTO();
        Profile.Section robot = file.get("Robot");
        if(robot.containsKey("robot.left")) {
            robotDTO.setLeft(robot.get("robot.left"));
        }
        if(robot.containsKey("robot.right")) {
            robotDTO.setRight(robot.get("robot.right"));
        }
        if(robot.containsKey("robot.distanceSensor")) {
            robotDTO.setDistanceSensor(robot.get("robot.distanceSensor"));
        }
        if(robot.containsKey("robot.connect")) {
            robotDTO.setConnect(Boolean.parseBoolean(robot.get("robot.connect")));
        }
        if(robot.containsKey("robot.shouldStopOnPhotos")) {
            robotDTO.setShouldStopOnPhotos(Boolean.parseBoolean(robot.get("robot.shouldStopOnPhotos")));
        }
        return robotDTO;
    }

    public void writeRobotDto(RobotDTO robotDTO) throws IOException {
        file.put("Robot", "robot.left", robotDTO.getLeft());
        file.put("Robot", "robot.right", robotDTO.getRight());
        file.put("Robot", "robot.distanceSensor", robotDTO.getDistanceSensor());
        file.put("Robot", "robot.connect", robotDTO.getConnect().toString());
        file.put("Robot", "robot.shouldStopOnPhotos", robotDTO.getShouldStopOnPhotos().toString());
        file.store();
    }

    public void writeAutophotosDTO(AutoPhotosDTO autoPhotosDTO) throws IOException {
        file.put("AutoPhotos", "autophotos.enabled", autoPhotosDTO.getAutoPhotosEnabled().toString());
        file.put("AutoPhotos", "autophotos.distance", autoPhotosDTO.getAutoPhotosDistance().toString());
        file.store();
    }

    public PhotoDTO readPhotoDTO() {
        PhotoDTO photoDTO = new PhotoDTO();
        Profile.Section photo = file.get("Photo");
        if(photo.containsKey("photo.width"))
            photoDTO.setWidth(Integer.parseInt(photo.get("photo.width")));
        if(photo.containsKey("photo.height"))
            photoDTO.setHeigth(Integer.parseInt(photo.get("photo.height")));
        if(photo.containsKey("photo.cameraType"))
            switch(photo.get("photo.cameraType")) {
                case "raspberry":
                    photoDTO.setCameraType(CameraType.RASPBERRY);
                    break;
                case "usb":
                    photoDTO.setCameraType(CameraType.USB);
                    break;
                default: throw new RuntimeException("Cannot read photo.cameraType");
            }
        return photoDTO;
    }

    public void writePhotoDTO(PhotoDTO photoDTO) throws IOException {
        file.put("Photo", "photo.width", photoDTO.getWidth());
        file.put("Photo", "photo.height", photoDTO.getHeigth());
        switch(photoDTO.getCameraType()) {
            case USB:
                file.put("Photo", "photo.cameraType", "usb");
                break;
            case RASPBERRY:
                file.put("Photo", "photo.cameraType", "raspberry");
                break;
        }
        file.store();
        this.photoDTO = photoDTO;
    }

    private NetworkDTO readNetworkDTO() {
        NetworkDTO networkDTO = new NetworkDTO();
        if (file.containsKey("Network")) {
            Profile.Section network = file.get("Network");
            if (network.containsKey("network.ssid"))
                networkDTO.setSsid(network.get("network.ssid"));
            if (network.containsKey("network.password"))
                networkDTO.setPassword(network.get("network.password"));
            if (network.containsKey("network.hotspot"))
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
            timer.setThreadState(ThreadState.NEW);
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
        if (file.containsKey("Database"))
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
                    throw new Exception("Cannot read database.type");
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
        Profile.Section savingPlaces;
        if (file.containsKey("SavingPlaces"))
            savingPlaces = file.get("SavingPlaces");
        else return savingPlacesDTO;
        if (savingPlaces.containsKey("savingPlaces.jpgComputerSave"))
            savingPlacesDTO.setJpgComputerSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgComputerSave")));
        if (savingPlaces.containsKey("savingPlaces.jpgRaspberryPendriveSave"))
            savingPlacesDTO.setJpgPendriveSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgRaspberryPendriveSave")));
        if (savingPlaces.containsKey("savingPlaces.jpgDatabaseSave"))
            savingPlacesDTO.setJpgDatabaseSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.jpgDatabaseSave")));
        if (savingPlaces.containsKey("savingPlaces.matPendriveSave"))
            savingPlacesDTO.setMatPendriveSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.matPendriveSave")));
        if (savingPlaces.containsKey("savingPlaces.matDatabaseSave"))
            savingPlacesDTO.setMatDatabaseSave(Boolean.parseBoolean(savingPlaces.get("savingPlaces.matDatabaseSave")));
        if (savingPlaces.containsKey("savingPlaces.jpgLocation") && savingPlaces.get("savingPlaces.jpgLocation") != null)
            savingPlacesDTO.setJpgComputerLocation(savingPlaces.get("savingPlaces.jpgLocation"));
        savingPlacesDTO.setDatabaseConfig(readDatabaseConfig());
        return savingPlacesDTO;
    }

    public void writeSavingPlaces(SavingPlacesDTO savingPlacesDTO) throws IOException {
        file.put("SavingPlaces", "savingPlaces.jpgComputerSave", savingPlacesDTO.getJpgComputerSave().toString());
        file.put("SavingPlaces", "savingPlaces.jpgRaspberryPendriveSave", savingPlacesDTO.getJpgPendriveSave().toString());
        file.put("SavingPlaces", "savingPlaces.jpgDatabaseSave", savingPlacesDTO.getJpgDatabaseSave().toString());
        file.put("SavingPlaces", "savingPlaces.jpgLocation", savingPlacesDTO.getJpgComputerLocation());
        file.put("SavingPlaces", "savingPlaces.matPendriveSave", savingPlacesDTO.getMatPendriveSave().toString());
        file.put("SavingPlaces", "savingPlaces.matDatabaseSave", savingPlacesDTO.getMatDatabaseSave().toString());
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
        if (file.containsKey("Security")) {
            security = file.get("Security");
        } else return usernameAndPasswordDTO;
        if (security.containsKey("security.enabled")) {
            usernameAndPasswordDTO.setEnabled(Boolean.parseBoolean(security.get("security.enabled")));
        }
        if (security.containsKey("security.username")) {
            usernameAndPasswordDTO.setUsername(security.get("security.username"));
        }
        if (security.containsKey("security.password")) {
            usernameAndPasswordDTO.setPassword(security.get("security.password"));
        }
        return usernameAndPasswordDTO;
    }
}
