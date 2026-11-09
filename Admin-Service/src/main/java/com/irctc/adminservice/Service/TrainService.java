package com.irctc.adminservice.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.adminservice.Entity.Seat;
import com.irctc.adminservice.Entity.Train;
import com.irctc.adminservice.Exception.BadRequestException;
import com.irctc.adminservice.Exception.ConflictException;
import com.irctc.adminservice.Exception.EventPublishingException;
import com.irctc.adminservice.Events.TrainCreatedEvent;
import com.irctc.adminservice.Repository.TrainRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrainService {

    private final TrainRepository trainRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Train createTrain(
            String trainNumber,
            String trainName,
            String coachName,
            List<Seat> seats) {

        
        if (trainRepository.existsByTrainNumber(trainNumber)) {
            throw new ConflictException(
                    "Train with this number already exists"
            );
        }

        
        if (seats == null || seats.isEmpty()) {
            throw new BadRequestException(
                    "At least one seat must be defined"
            );
        }

        
        Set<Integer> seatNumbers = new HashSet<>();

        for (Seat seat : seats) {
            if (seat.getSeatNumber() == null) {
                throw new BadRequestException(
                        "Seat number is required"
                );
            }

            if (!seatNumbers.add(seat.getSeatNumber())) {
                throw new BadRequestException(
                        "Duplicate seat numbers found"
                );
            }

            if (seat.getSeatType() == null) {
                throw new BadRequestException(
                        "Seat type is required"
                );
            }

            if (seat.getPrice() == null || seat.getPrice() < 0) {
                throw new BadRequestException(
                        "Seat price must be zero or greater"
                );
            }
        }

        
        Train train = new Train();
        train.setTrainNumber(trainNumber);
        train.setTrainName(trainName);
        train.setCoachName(
                coachName == null || coachName.isBlank()
                        ? "AC"
                        : coachName
        );
        train.setTotalSeats(seats.size());

        
        for (Seat seat : seats) {
            seat.setId(null);
            seat.setTrain(train);
        }

        train.setSeats(seats);

        
        Train savedTrain = trainRepository.save(train);

        log.info(
                "Train created: id={}, trainNumber={}, totalSeats={}",
                savedTrain.getId(),
                savedTrain.getTrainNumber(),
                savedTrain.getTotalSeats()
        );

        
        publishTrainCreated(savedTrain);

        return savedTrain;
    }

    private void publishTrainCreated(Train savedTrain) {

    try {
        TrainCreatedEvent event = new TrainCreatedEvent();

        event.setEventType("TRAIN_CREATED");
        event.setData(savedTrain);
        event.setTimestamp(Instant.now());

        String eventString = objectMapper.writeValueAsString(event);

        log.info(
                "Sending TrainCreatedEvent to Kafka for trainId: {}",
                savedTrain.getId()
        );

        kafkaTemplate.send(
                "train-created",
                "train-" + savedTrain.getId(),
                eventString
        );

        log.info(
                "TrainCreatedEvent sent to Kafka for trainId: {}",
                savedTrain.getId()
        );

    } catch (Exception e) {
        log.error(
                "Failed to send TrainCreatedEvent to Kafka for trainId: {}",
                savedTrain.getId(),
                e
        );

        throw new EventPublishingException(
                "Failed to send TrainCreatedEvent to Kafka for trainId: "
                        + savedTrain.getId(),
                e
        );
      }
    }


}