
package com.irctc.adminservice.DTO;

import com.irctc.adminservice.Entity.Station;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStationResponseDTO {

    private boolean success;
    private String message;
    private Station data;
}
