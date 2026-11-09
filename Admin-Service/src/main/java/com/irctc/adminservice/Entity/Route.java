package com.irctc.adminservice.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @UuidGenerator
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false, unique = true)
    private Train train;

    @OneToMany(
        mappedBy = "route",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<RouteStation> routeStations = new ArrayList<>();
}
