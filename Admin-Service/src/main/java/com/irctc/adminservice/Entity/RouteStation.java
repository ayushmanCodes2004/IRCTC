package com.irctc.adminservice.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
    name = "route_stations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"route_id", "sequence_number"}),
        @UniqueConstraint(columnNames = {"route_id", "station_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStation {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private Integer sequenceNumber;

    private String arrivalTime;

    private String departureTime;

    @Column(nullable = false)
    @Builder.Default
    private Double distanceFromOrigin = 0.0;
}
