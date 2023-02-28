package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.repository.SamRepository;
import org.w3c.dom.Text;

import javax.persistence.*;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"Temps_ms"})
@Entity
@Data
@Table(name = "SAM005")
public class Sam {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;


    @JsonProperty("NbEssieux")
    private  Integer Nb_Essieux;

    @ElementCollection
    @JsonProperty("NbOccultations")
    private List<Integer> NbOccultations;


    @Column(columnDefinition = "Varchar")
    private String fileName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "train_id", referencedColumnName = "id")
    private Train train;

    public Sam(Long id, Integer nb_Essieux, List<Integer> nbOccultations, String fileName, Train train, Double vitesse1_7, Double vitesse2_8, Double vitesse_moy) {
        this.id = id;
        Nb_Essieux = nb_Essieux;
        NbOccultations = nbOccultations;
        this.fileName = fileName;
        this.train = train;
        this.vitesse1_7 = vitesse1_7;
        this.vitesse2_8 = vitesse2_8;
        this.vitesse_moy = vitesse_moy;
    }

    public List<Integer> getNbOccultations() {
        return NbOccultations;
    }

    public void setNbOccultations(List<Integer> nbOccultations) {
        NbOccultations = nbOccultations;
    }

    public Sam() {
        loadFilenamesStartingWith50592();


    }
    public void loadFilenamesStartingWith50592() {

        String path = "C:\\Users\\Ilham Barache\\Documents\\input";
        File directory = new File(path);
        List<File> files = List.of(directory.listFiles())
                .stream()
                .filter(f -> f.getName().startsWith("SAM") && f.getName().endsWith(".json"))
                .collect(Collectors.toList());

        List<String> filenames = new ArrayList<>();
        for (File file : files) {
            String filename = file.getName();
            filenames.add(filename);
        }

        for (String filename : filenames) {
            this.setFileName(filename);
        }
    }


    public Integer getNb_Essieux() {
        return Nb_Essieux;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getNbEssieux() {
        return Nb_Essieux;
    }

    public void setNb_Essieux(Integer nbEssieux) {
        Nb_Essieux = nbEssieux;
    }

    @JsonProperty("Vitesses_1-7_km/h")
    private Double vitesse1_7 ;
    @JsonProperty("Vitesses_2-8_km/h")
    private Double vitesse2_8;
    @JsonProperty("Vitesse_moyenne_km/h")
    private Double vitesse_moy;

    @JsonIgnore
    @Embedded
    private  Temps_ms temps_ms;








    public Double getVitesse1_7() {
        return vitesse1_7;
    }



    public Double getVitesse2_8() {
        return vitesse2_8;
    }

    public Double getVitesse_moy() {
        return vitesse_moy;
    }



    public void setVitesse1_7(Double vitesse1_7) {
        this.vitesse1_7 = vitesse1_7;
    }

    public void setVitesse2_8(Double vitesse2_8) {
        this.vitesse2_8 = vitesse2_8;
    }


    public void setVitesse_moy(Double vitesse_moy) {
        this.vitesse_moy = vitesse_moy;


    }



    public Sam(Long id, Double vitesse1_7, double vitesse2_8, double vitesse_moy) {
        this.id = id;
        this.vitesse1_7 = vitesse1_7;
        this.vitesse2_8 = vitesse2_8;
        this.vitesse_moy = vitesse_moy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sam(Double vitesse1_7, Double vitesse2_8, Double vitesse_moy) {
        this.vitesse1_7 = vitesse1_7;
        this.vitesse2_8 = vitesse2_8;
        this.vitesse_moy = vitesse_moy;
    }
}
