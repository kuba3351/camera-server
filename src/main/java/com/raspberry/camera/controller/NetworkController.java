package com.raspberry.camera.controller;

import com.raspberry.camera.dto.NetworkDTO;
import com.raspberry.camera.service.ConfigFileService;
import com.raspberry.camera.service.NetworkService;
import com.raspberry.camera.dto.NetworkViewDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
public class NetworkController {

    private NetworkService networkService;

    private final static Logger logger = Logger.getLogger(NetworkController.class);

    @Autowired
    public NetworkController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @GetMapping("/api/network/getNetworkInfo")
    public NetworkDTO getNetworkInfo() {
        logger.info("Żądanie pobrania ustawień sieci...");
        NetworkDTO networkDTOFromConfig = networkService.getNetworkDTO();
        NetworkDTO responseNetworkDTO = new NetworkDTO();
        responseNetworkDTO.setSsid(networkDTOFromConfig.getSsid());
        return responseNetworkDTO;
    }

    @PostMapping("/api/network")
    public ResponseEntity connectToNetwork(@RequestBody @Valid NetworkDTO networkDTO) throws IOException, InterruptedException {
        networkService.setNetworkDTO(networkDTO);
        logger.info("Zmieniono ustawień sieci. Wymagany restart.");
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/api/network/checkAvailableWifi")
    public List<NetworkViewDTO> checkWifi() throws IOException, InterruptedException {
        return networkService.checkAvailableNetworks();
    }
}
