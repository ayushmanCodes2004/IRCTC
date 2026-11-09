package com.irctc.adminservice.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "trains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Train {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false, unique = true)
    private String trainNumber;

    @Column(nullable = false)
    private String trainName;

    @Column(nullable = false)
    @Builder.Default
    private String coachName = "AC";

    @Column(nullable = false)
    private Integer totalSeats;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @OneToOne(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    private Route route;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
