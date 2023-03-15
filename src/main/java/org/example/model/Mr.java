package org.example.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "MR")

public class Mr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "mr",columnDefinition = "Varchar")
    private String mr;


    @Column(name = "numTrain",columnDefinition = "Varchar")
    private  String  numTrain;


    public String getMr() {
        return mr;
    }

    public void setMr(String mr) {
        this.mr = mr;
    }

    public String getNumTrain() {
        return numTrain;
    }

    public void setNumTrain(String numTrain) {
        this.numTrain = numTrain;
    }
}
