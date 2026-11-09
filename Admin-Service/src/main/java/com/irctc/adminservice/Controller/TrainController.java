package com.irctc.adminservice.Controller;

import com.irctc.adminservice.DTO.CreateTrainRequestDTO;
import com.irctc.adminservice.DTO.CreateTrainResponseDTO;
import com.irctc.adminservice.Entity.Train;
import com.irctc.adminservice.Service.TrainService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @PostMapping
    public ResponseEntity<CreateTrainResponseDTO> createTrain(
            @Valid @RequestBody CreateTrainRequestDTO request) {

        Train train = trainService.createTrain(
                request.getTrainNumber(),
                request.getTrainName(),
                request.getCoachName(),
                request.getSeats()
        );

        CreateTrainResponseDTO response = new CreateTrainResponseDTO(
                true,
                "Train added successfully",
                train
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

