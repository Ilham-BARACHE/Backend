package org.example.repository;

import org.example.model.Mr;
import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface MrRepository extends JpaRepository<Mr,Long> {

    Mr findByNumTrain(String numTrain);

    List<Mr> findByNumTrainIn(List<String> numTrains);

List<Mr> findAll();
}
