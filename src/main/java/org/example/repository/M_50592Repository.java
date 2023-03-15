package org.example.repository;

import org.example.model.M_50592;
import org.example.model.Sam;
import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface M_50592Repository extends JpaRepository<M_50592, Long> {

    List<M_50592> findBySiteAndDateFichier(String site, Date dateFichier);
}
