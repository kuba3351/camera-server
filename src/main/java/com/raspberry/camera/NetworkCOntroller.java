package com.raspberry.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class NetworkCOntroller {

    @Autowired
    private ConfigFileService configFileService;

    @Autowired
    private NetworkService networkService;

    @GetMapping("/api/network/getNetworkInfo")
    public NetworkDTO getNetworkInfo() {
        return configFileService.getNetworkDTO();
    }

    @PostMapping("/api/network/connectToNetwork")
    public ResponseEntity connectToNetwork(@RequestBody NetworkDTO networkDTO) throws IOException, InterruptedException {
        if(networkService.connectToNetwork(networkDTO)) {
            configFileService.writeNetworkDTO(networkDTO);
            return new ResponseEntity(HttpStatus.OK);
        }
        if(networkService.getHotspotActive())
            networkService.bringUpHotspot();
        else
            networkService.connectToNetwork(configFileService.getNetworkDTO());
        return new ResponseEntity(HttpStatus.CONFLICT);
    }
}
