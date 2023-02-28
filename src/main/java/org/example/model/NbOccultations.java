package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data

public class NbOccultations {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @JsonProperty("NbOccultations")
    private Integer NbOccultations;

    public NbOccultations() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Integer getNbOccultations() {
        return NbOccultations;
    }

    public void setNbOccultations(Integer nbOccultations) {
        NbOccultations = nbOccultations;
    }


    public NbOccultations(Long id, Integer nbOccultations) {
        this.id = id;
        NbOccultations = nbOccultations;
    }
}
