package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

@Service
public class NetworkService {

    ConfigFileService configFileService;

    public NetworkDTO networkDTO;

    private final static Logger logger = Logger.getLogger(NetworkService.class);

    private Boolean isHotspotActive;

    public Boolean getHotspotActive() {
        return isHotspotActive;
    }

    @Autowired
    public NetworkService(ConfigFileService configFileService) throws IOException, InterruptedException {
        this.configFileService = configFileService;
        this.networkDTO = configFileService.getNetworkDTO();
        if(!isConnectedTo(networkDTO.getSsid())) {
            logger.info("Połączenie nieaktywne. Próbuję łączyć według ustawień...");
            if(!connectToNetwork(networkDTO)) {
                logger.info("Nie udało się połączyć. Stawiam hotspota...");
                bringUpHotspot();
            }
        }
        else {
            logger.info("Połączenie już nawiązane. Kontynuuję uruchamianie...");
            isHotspotActive = false;
        }
    }

    public void bringUpHotspot() throws IOException, InterruptedException {
        Process hotspot = Runtime.getRuntime().exec("nmcli c up hotspot");
        if(hotspot.waitFor() != 0) {
            logger.error("Stawianie hotspota nieudane.");
            isHotspotActive = false;
        }
        else {
            logger.info("Hotspot postawiony!");
            Process dhcpcd = Runtime.getRuntime().exec("sudo systemctl start isc-dhcp-server");
            if(dhcpcd.waitFor() == 0) {
                logger.info("DHCP postawiony!");
                isHotspotActive = true;
            }
            else {
                logger.error("Nie udało się postawić DHCP");
                isHotspotActive = false;
            }
        }
    }

    public Boolean connectToNetwork(NetworkDTO networkDTO) throws IOException, InterruptedException {
        String password = networkDTO.getPassword();
        String command = "nmcli dev wifi connect "+networkDTO.getSsid();
        if(password != null && !password.isEmpty())
            command += " password "+password;
        Process connecting = Runtime.getRuntime().exec(command);
        connecting.waitFor();
        if(isConnectedTo(networkDTO.getSsid())) {
            this.networkDTO = networkDTO;
            isHotspotActive = false;
            return true;
        }
        return false;
    }

    private Boolean isConnectedTo(String name) throws IOException, InterruptedException {
        logger.info("Sprawdzanie aktywnego połączenia...");
        Process process = Runtime.getRuntime().exec("nmcli -f in-use,ssid dev wifi list");
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.lines().filter(line -> line.startsWith("*"))
                .map(line -> line.substring(1)).map(String::trim).anyMatch(line -> line.equals(name));
    }

    private NetworkViewDTO mapStringToViewDTO(String line) {
        if(line.startsWith("*"))
            line = line.substring(1);
        Scanner scanner = new Scanner(line);
        NetworkViewDTO networkViewDTO = new NetworkViewDTO();
        networkViewDTO.setSsid(scanner.next());
        networkViewDTO.setMode(scanner.next());
        networkViewDTO.setChannel(scanner.nextInt());
        networkViewDTO.setMark(scanner.next()+" "+scanner.next());
        networkViewDTO.setSignal(scanner.nextInt());
        networkViewDTO.setBars(scanner.next());
        networkViewDTO.setSecurity(scanner.next());
        return networkViewDTO;
    }
}
