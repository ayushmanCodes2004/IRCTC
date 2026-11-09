package com.irctc.adminservice.Repository;

import com.irctc.adminservice.Entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {

    boolean existsByCode(String code);
}