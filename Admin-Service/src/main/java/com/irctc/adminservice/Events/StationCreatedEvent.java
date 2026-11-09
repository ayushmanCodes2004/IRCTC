package com.irctc.adminservice.Events;

import com.irctc.adminservice.Entity.Station;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationCreatedEvent {

    private String eventType;
    private Station data;
    private Instant timestamp;
}
