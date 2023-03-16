package org.example.repository;


import org.example.model.Sam;
import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Temporal;

import javax.persistence.TemporalType;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {

    List<Train> findByDateFichierAndSite(Date dateFichier, String site);

    List<Train> findBySiteAndDateFichier(String site, Date dateFichier);

    List<Train> findAll();

}
