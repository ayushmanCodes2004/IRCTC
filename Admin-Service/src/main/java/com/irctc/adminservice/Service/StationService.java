package com.irctc.adminservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.adminservice.Entity.Station;
import com.irctc.adminservice.Events.StationCreatedEvent;
import com.irctc.adminservice.Repository.StationRepository;
import com.irctc.adminservice.Exception.ConflictException;
import org.springframework.kafka.core.KafkaTemplate;
import com.irctc.adminservice.Exception.EventPublishingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;


@RequiredArgsConstructor
@Slf4j
@Service
public class StationService {

    private final StationRepository stationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public Station createStation(
            String code,
            String name,
            String city,
            String state) {

        if (stationRepository.existsByCode(code)) {
            throw new ConflictException("Station code already exists");
        }

        Station station = new Station();
        station.setCode(code);
        station.setName(name);
        station.setCity(city);
        station.setState(state);

        Station savedStation = stationRepository.save(station);

        log.info(
                "Station Created: id={}, code={}",
                savedStation.getId(),
                savedStation.getCode()
        );

        publishStationCreated(savedStation);

        return savedStation;
    }

    private void publishStationCreated(Station savedStation) {
        try {
            StationCreatedEvent event = new StationCreatedEvent();

            event.setEventType("STATION_CREATED");
            event.setData(savedStation);
            event.setTimestamp(Instant.now());

            String eventString = objectMapper.writeValueAsString(event);

            log.info(
                    "Sending StationCreatedEvent to Kafka for stationId: {}",
                    savedStation.getId()
            );

            kafkaTemplate.send("station-created", eventString);

            log.info(
                    "StationCreatedEvent sent to Kafka for stationId: {}",
                    savedStation.getId()
            );

        } catch (Exception e) {
            log.error(
                    "Failed to send StationCreatedEvent to Kafka for stationId: {}",
                    savedStation.getId(),
                    e
            );

            throw new EventPublishingException(
                    "Failed to send StationCreatedEvent to Kafka for stationId: "
                            + savedStation.getId(),
                    e
            );
        }
    }
}

