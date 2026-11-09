package com.irctc.adminservice.Repository;

import com.irctc.adminservice.Entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, String> {

    boolean existsByTrainNumber(String trainNumber);
}