package com.irctc.adminservice.DTO;

import com.irctc.adminservice.Entity.Seat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainRequestDTO {

    @NotBlank(message = "Train number is required")
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    private String trainName;

    @NotBlank(message = "Coach name is required")
    private String coachName;

    @NotEmpty(message = "At least one seat must be defined")
    @Valid
    private List<Seat> seats;
}

