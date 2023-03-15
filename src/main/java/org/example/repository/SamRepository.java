package org.example.repository;

import org.example.model.Sam;
import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.TemporalType;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface SamRepository extends JpaRepository<Sam, Long> {
//    List<Sam> findByDateBetween(Date startDate, Date endDate);

    List<Sam> findBySiteAndDateFichier(String site, Date dateFichier);

}
