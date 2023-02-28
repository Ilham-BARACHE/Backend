package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.example.repository.M_50592Repository;

import javax.persistence.*;
import java.io.File;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"BE_R1","BE_R2","BL_R1","BL_R2","FFT_R1","FFT_R2","ParametresBL","ParametresBE"})
@Entity
@Data
@Table(name = "M50592")
public class M_50592 {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(columnDefinition = "Varchar")
    private String fileName;
    @Column(name = "date_fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;
    @Column(name = "ville_depart", insertable = false, updatable = false)
    private String villeDepart;

    @Column(name = "ville_arrivee", insertable = false, updatable = false)
    private String villeArrivee;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public java.util.Date getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(java.sql.Date dateFichier) {
        this.dateFichier = dateFichier;
    }

    public java.util.Date getHeureFichier() {
        return heureFichier;
    }

    public void setHeureFichier(Time heureFichier) {
        this.heureFichier = heureFichier;
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

    public M_50592() {
        loadFilenamesStartingWith50592();

loadStartingWith50592(fileName);
    }

    public M_50592(Long id, String fileName, Date dateFichier, Date heureFichier) {
        this.id = id;
        this.fileName = fileName;
        this.dateFichier = dateFichier;
        this.heureFichier = heureFichier;
    }

    public void loadFilenamesStartingWith50592() {

        String path = "C:\\Users\\Ilham Barache\\Documents\\input";
        File directory = new File(path);
        List<File> files = List.of(directory.listFiles())
                .stream()
                .filter(f -> f.getName().startsWith("50592") && f.getName().endsWith(".json"))
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

    public void loadStartingWith50592(String fileName) {
        String[] parts = fileName.split("_"); // Diviser le nom de fichier en parties en utilisant le caractère '_'
        if (parts.length > 1) { // Vérifier si le nom de fichier a été correctement divisé
            String dateTimePart = parts[1]; // Récupérer la partie qui contient la date et l'heure
            String[] dateTimeParts = dateTimePart.split("[HhMmSs]"); // Diviser la partie date-heure en parties en utilisant les caractères 'HhMmSs' comme séparateurs
            if (dateTimeParts.length == 6) { // Vérifier si la partie date-heure a été correctement divisée
                String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2]; // Concaténer les parties pour former la date
                String heurePart = dateTimeParts[3] + ":" + dateTimeParts[4] + ":" + dateTimeParts[5]; // Concaténer les parties pour former l'heure

                // Convertir la date et l'heure en objets Date et Time
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                    java.util.Date parsedDate = dateFormat.parse(datePart);
                    java.sql.Date date = new java.sql.Date(parsedDate.getTime());

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    java.util.Date parsedTime = timeFormat.parse(heurePart);
                    java.sql.Time time = new java.sql.Time(parsedTime.getTime());

                    // Mettre à jour les champs dateFichier et heureFichier de l'objet M_50592
                    this.setDateFichier(date);
                    this.setHeureFichier(time);
                    this.setFileName(fileName);
                } catch (ParseException e) {
                    // Gérer l'exception si la date ou l'heure ne peut pas être analysée
                    e.printStackTrace();
                }

            }
        }
    }


    public M_50592(String fileName, Environnement environnement) {
        this.fileName = fileName;
        this.environnement = environnement;

    }



    @JsonProperty("Environnement")
    @Embedded
    private Environnement environnement;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "train_id", referencedColumnName = "id")
    private Train train;

    @JsonIgnore
    @Embedded
    private BE_R1 be ;

    @JsonIgnore
    @Embedded
    private BE_R2 beR22;

    @JsonIgnore
    @Embedded
    private BL_R1 blR1;

    @JsonIgnore
    @Embedded
    private BL_R2 blR2;

    @JsonIgnore
    @Embedded
    private FFT_R1 fftR1;

    @JsonIgnore
    @Embedded
    private FFT_R2 fftR2;

    @JsonIgnore
    @Embedded
    private ParametresBL parametresBL;

    @JsonIgnore
    @Embedded
    private ParametresBE parametresBE;


    public Environnement getEnvironnement() {
        return environnement;
    }

    public void setEnvironnement(Environnement environnement) {
        this.environnement = environnement;

    }
}
