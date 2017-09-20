package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;
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


    @Autowired
    public NetworkService(ConfigFileService configFileService) throws IOException, InterruptedException {
        this.configFileService = configFileService;
        this.networkDTO = configFileService.getNetworkDTO();
        BufferedReader bufferedReader = checkActiveConnection();
        if(bufferedReader.lines().map(Scanner::new).noneMatch(line -> line.next().equals(networkDTO.getSsid()))) {
            logger.info("Połączenie nieaktywne. Próbuję łączyć według ustawień...");
            String command = null;
            String password = networkDTO.getPassword();
            if(password == null || password.isEmpty())
                command = "nmcli dev wifi connect "+networkDTO.getSsid();
            else
                command = "nmcli dev wifi connect "+networkDTO.getSsid()+" password "+password;
            Process connecting = Runtime.getRuntime().exec(command);
            connecting.waitFor();
            BufferedReader reader2 = checkActiveConnection();
            if(reader2.lines().map(Scanner::new).noneMatch(line -> line.next().equals(networkDTO.getSsid()))) {
                logger.info("Nie udało się połączyć. Stawiam hotspota...");
                Process hotspot = Runtime.getRuntime().exec("nmcli c up hotspot");
                if(hotspot.waitFor() != 0)
                    logger.error("Stawianie hotspota nieudane.");
                else {
                    logger.info("Hotspot postawiony!");
                    Process dhcpcd = Runtime.getRuntime().exec("sudo systemctl start isc-dhcp-server");
                    if(dhcpcd.waitFor() == 0)
                        logger.info("DHCP postawiony!");
                    else
                        logger.error("Nie udało się postawić DHCP");
                }
            }
        }
        logger.info("Połączenie już nawiązane. Kontynuuję uruchamianie...");
    }

    private BufferedReader checkActiveConnection() throws IOException, InterruptedException {
        logger.info("Sprawdzanie aktywnego połączenia...");
        Process process = Runtime.getRuntime().exec("nmcli c show --active");
        process.waitFor();
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
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
