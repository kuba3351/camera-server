package com.raspberry.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
public class NetworkController {

    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private NetworkService networkService;

    private final int BUFFER_SIZE = 1000;

    @GetMapping("/api/network/getNetworkInfo")
    public NetworkDTO getNetworkInfo() {
        NetworkDTO networkDTOFromConfig = configFileService.getNetworkDTO();
        NetworkDTO responseNetworkDTO = new NetworkDTO();
        responseNetworkDTO.setSsid(networkDTOFromConfig.getSsid());
        return responseNetworkDTO;
    }

    @PostMapping("/api/network/connectToNetwork")
    public ResponseEntity connectToNetwork(@RequestBody NetworkDTO networkDTO) throws IOException, InterruptedException {
        Thread thread = new Thread(() -> {
            String password = networkDTO.getPassword();
            if(password == null) {
                networkDTO.setPassword(configFileService.getNetworkDTO().getPassword());
            }
            try {
                if(networkService.connectToNetwork(networkDTO)) {
                    configFileService.writeNetworkDTO(networkDTO);
                }
                if(networkService.getHotspotActive())
                    networkService.enableHotspot();
                else
                    networkService.connectToNetwork(configFileService.getNetworkDTO());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        Thread.sleep(5000);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/network/enableHotspot")
    public ResponseEntity enableHotspot() throws IOException, InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                if (!networkService.enableHotspot()) {
                    networkService.connectToNetwork(configFileService.getNetworkDTO());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        Thread.sleep(5000);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/network/checkAvailableWifi")
    public List<NetworkViewDTO> checkWifi() throws IOException, InterruptedException {
        return networkService.checkAvailableNetworks();
    }
}
