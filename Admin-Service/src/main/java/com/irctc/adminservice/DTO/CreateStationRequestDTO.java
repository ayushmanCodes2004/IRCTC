
package com.irctc.adminservice.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStationRequestDTO {

    @NotBlank(message = "Station name is required")
    private String name;

    @NotBlank(message = "Station code is required")
    private String code;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;
}
