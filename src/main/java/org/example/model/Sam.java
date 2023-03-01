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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    @Column(name = "date_fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;


    public Sam(Long id, Integer nb_Essieux, List<Integer> nbOccultations, String fileName, Double vitesse1_7, Double vitesse2_8, Double vitesse_moy) {
        this.id = id;
        Nb_Essieux = nb_Essieux;
        NbOccultations = nbOccultations;
        this.fileName = fileName;

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
    public void loadStartingWithSam(String fileName) {
        int index = fileName.indexOf("_");
        if (index > 0) { // Vérifier si le nom de fichier contient au moins un "_"
            // Trouver l'index du 2ème "_" en partant de la droite
            int lastIndex = fileName.lastIndexOf("_");
            if (lastIndex > index) {
                String dateTimePart = fileName.substring(index+1, fileName.length()-5); // Extraire la partie qui contient la date et l'heure en excluant l'extension du fichier (.json)

                System.out.println("dateTimePart: " + dateTimePart);

                String[] dateTimeParts = dateTimePart.split("[_ .hms]+");
                System.out.println("dateTimeParts: " + Arrays.toString(dateTimeParts));

                if (dateTimeParts.length == 6) { // Vérifier si la partie date-heure a été correctement divisée
                    String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2]; // Concaténer les parties pour former la date
                    String heurePart = dateTimeParts[3] + "h" + dateTimeParts[4] + "m" + dateTimeParts[5]+ "s"; // Concaténer les parties pour former l'heure
                    System.out.println("datePart: " + datePart); // Ajouter un log pour afficher la partie date
                    System.out.println("heurePart: " + heurePart); // Ajouter un log pour afficher la partie heure

                    // Convertir la date et l'heure en objets Date et Time
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                        java.util.Date parsedDate = dateFormat.parse(datePart);
                        java.sql.Date date = new java.sql.Date(parsedDate.getTime());

                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh'h'mm'm'ss's'");
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
    }

    public Date getDateFichier() {
        return dateFichier;
    }

    public void setDateFichier(Date dateFichier) {
        this.dateFichier = dateFichier;
    }

    public Date getHeureFichier() {
        return heureFichier;
    }

    public void setHeureFichier(Date heureFichier) {
        this.heureFichier = heureFichier;
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
