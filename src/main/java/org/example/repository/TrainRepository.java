package org.example.repository;

import org.example.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {

}
