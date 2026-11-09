package com.irctc.adminservice.Events;

import com.irctc.adminservice.Entity.Train;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainCreatedEvent {

    private String eventType;
    private Train data;
    private Instant timestamp;
}