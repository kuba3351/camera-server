package com.raspberry.camera.service;

import com.raspberry.camera.dto.NetworkDTO;
import com.raspberry.camera.dto.NetworkViewDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class NetworkService {

    private NetworkDTO networkDTO;

    private final static Logger logger = Logger.getLogger(NetworkService.class);

    private Boolean isHotspotActive;

    public Boolean getHotspotActive() {
        return isHotspotActive;
    }

    @Autowired
    public NetworkService(ConfigFileService configFileService) throws IOException, InterruptedException {
        this.networkDTO = configFileService.getNetworkDTO();
        if(networkDTO.getHotspot())
            enableHotspot();
        else {
            if (!isConnectedTo(networkDTO.getSsid())) {
                logger.info("Połączenie nieaktywne. Próbuję łączyć według ustawień...");
                if (!connectToNetwork(networkDTO)) {
                    logger.info("Nie udało się połączyć. Stawiam hotspota...");
                    enableHotspot();
                }
            } else {
                logger.info("Połączenie już nawiązane. Kontynuuję uruchamianie...");
                isHotspotActive = false;
            }
        }
    }

    public Boolean enableHotspot() throws IOException, InterruptedException {
        Process hotspot = Runtime.getRuntime().exec("sudo nmcli c up hotspot");
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
        return isHotspotActive;
    }

    public Boolean connectToNetwork(NetworkDTO networkDTO) throws IOException, InterruptedException {
        Process dhcpcd = Runtime.getRuntime().exec("sudo systemctl stop isc-dhcp-server");
        dhcpcd.waitFor();
        String password = networkDTO.getPassword();
        String command = "sudo nmcli dev wifi connect "+networkDTO.getSsid();
        if(password != null && !password.isEmpty())
            command += " password "+password;
        Process connecting = Runtime.getRuntime().exec(command);
        connecting.waitFor();
        new BufferedReader(new InputStreamReader(connecting.getInputStream())).lines().forEach(System.out::println);
        new BufferedReader(new InputStreamReader(connecting.getErrorStream())).lines().forEach(System.out::println);
        Thread.sleep(5000);
        if(isConnectedTo(networkDTO.getSsid())) {
            this.networkDTO = networkDTO;
            isHotspotActive = false;
            return true;
        }
        return false;
    }

    private Boolean isConnectedTo(String name) throws IOException, InterruptedException {
        logger.info("Sprawdzanie aktywnego połączenia...");
        Process process = Runtime.getRuntime().exec("nmcli -f name c show --active");
        process.waitFor();
        Scanner scanner = new Scanner(process.getInputStream());
        scanner.nextLine();
        return scanner.hasNextLine() && scanner.nextLine().contains(name);
    }

    public List<NetworkViewDTO> checkAvailableNetworks() throws IOException, InterruptedException {
        logger.info("Sprawdzanie dostępnych połączeń wifi...");
        Process process = Runtime.getRuntime().exec("sudo nmcli -f SSID,BARS,SECURITY -m multiline dev wifi list");
        process.waitFor();
        InputStream inputStream = process.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        ArrayList<NetworkViewDTO> networkViewDTOS = new ArrayList<>();
        int linesCount = 0;
        NetworkViewDTO networkViewDTO = new NetworkViewDTO();
        while(scanner.hasNextLine()) {
            linesCount++;
            String id = scanner.next();
            String value = scanner.nextLine().trim();
            switch(id) {
                case "SSID:":
                    networkViewDTO.setSsid(value);
                    break;
                case "PASKI:":
                    networkViewDTO.setBars(value);
                    break;
                case "ZABEZPIECZENIA:":
                    networkViewDTO.setSecurity(value);
                    break;
                default: throw new RuntimeException("Problem ze sparsowaniem wyjścia.");
            }
            if(linesCount % 3 == 0) {
                networkViewDTOS.add(networkViewDTO);
                networkViewDTO = new NetworkViewDTO();
            }
        }
        return networkViewDTOS;
    }
}
