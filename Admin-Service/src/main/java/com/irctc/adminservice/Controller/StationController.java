package com.irctc.adminservice.Controller;

import com.irctc.adminservice.DTO.CreateStationRequestDTO;

import com.irctc.adminservice.DTO.CreateStationResponseDTO;
import com.irctc.adminservice.Entity.Station;
import com.irctc.adminservice.Service.StationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

   @PostMapping
   public ResponseEntity<CreateStationResponseDTO> createStation(
        @Valid @RequestBody CreateStationRequestDTO request) {

        Station station = stationService.createStation(
            request.getCode().toUpperCase(),
            request.getName(),
            request.getCity(),
            request.getState()
    );

    CreateStationResponseDTO response = new CreateStationResponseDTO(
            true,
            "Station Created Successfully",
            station
    );

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

}