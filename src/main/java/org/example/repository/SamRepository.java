package org.example.repository;

import org.example.model.Sam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SamRepository extends JpaRepository<Sam, Long> {

}
