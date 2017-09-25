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
        if(networkService.connectToNetwork(networkDTO)) {
            configFileService.writeNetworkDTO(networkDTO);
            return new ResponseEntity(HttpStatus.OK);
        }
        if(networkService.getHotspotActive())
            networkService.enableHotspot();
        else
            networkService.connectToNetwork(configFileService.getNetworkDTO());
        return new ResponseEntity(HttpStatus.CONFLICT);
    }

    @GetMapping("/api/network/enableHotspot")
    public ResponseEntity enableHotspo() throws IOException, InterruptedException {
        if(networkService.enableHotspot()) {
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/api/network/checkAvailableWifi")
    public List<NetworkViewDTO> checkWifi() throws IOException, InterruptedException {
        return networkService.checkAvailableNetworks();
    }
}
