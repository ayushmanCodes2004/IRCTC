package com.irctc.adminservice.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.irctc.adminservice.Enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"train_id", "seat_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    @JsonIgnore
    private Train train;
}
