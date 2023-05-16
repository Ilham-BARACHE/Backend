package org.example.repository;

import org.example.model.Mr;
import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MrRepository extends JpaRepository<Mr,Long> {


    Mr findByNumTrain(String numTrain);

    List<Mr> findAllByNumTrain(String numTrain);

    List<Mr> findDistinctByNumTrain(String numTrain);




    List<Mr> findByMr(String typeMr);



List<Mr> findAll();
}
