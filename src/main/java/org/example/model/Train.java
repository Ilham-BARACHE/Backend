package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;


@Entity
@Data
@Table(name = "TRAIN")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private  Integer Nb_Essieux;







    @Column(name = "ville_depart")
    private String villeDepart;

    @Column(name = "ville_arrivee")
    private String villeArrivee;

    public Train() {

    }

    public Integer getNb_Essieux() {
        return Nb_Essieux;
    }

    public void setNb_Essieux(Integer nb_Essieux) {
        Nb_Essieux = nb_Essieux;
    }

    public M_50592 getM_50592() {
        return m_50592;
    }

    public void setM_50592(M_50592 m_50592) {
        this.m_50592 = m_50592;
    }

    public String getVilleDepart() {
        return villeDepart;
    }

    public void setVilleDepart(String villeDepart) {
        this.villeDepart = villeDepart;
    }

    public String getVilleArrivee() {
        return villeArrivee;
    }

    public void setVilleArrivee(String villeArrivee) {
        this.villeArrivee = villeArrivee;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "m_50592_id", referencedColumnName = "id")
    private M_50592 m_50592;

    @OneToOne(mappedBy = "train", cascade = CascadeType.ALL)
    private Sam sam;
    @PreUpdate
    public void updateNbEssieux() {
        if (sam != null) {
            Nb_Essieux = sam.getNbEssieux();
        }
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Train(Long id, Integer nb_Essieux, String villeDepart, String villeArrivee, M_50592 m_50592) {
        this.id = id;
        Nb_Essieux = nb_Essieux;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.m_50592 = m_50592;
    }

    public void setVillesNomFromM50592() {
        if (m_50592 != null) {
            villeDepart = m_50592.getVilleDepart();
            villeArrivee = m_50592.getVilleArrivee();
        }
    }
}
