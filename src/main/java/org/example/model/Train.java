package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Data
@Table(name = "TRAIN")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("Num_train")
    private  Integer numTrain;
    @Column(columnDefinition = "Varchar")
    private String fileName;
    @Column(name = "date_fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;






    public void loadFilenamesStartingWithTrain() {

        String path = "C:\\Users\\Ilham Barache\\Documents\\input";
        File directory = new File(path);
        List<File> files = List.of(directory.listFiles())
                .stream()
                .filter(f -> f.getName().startsWith("TRAIN") && f.getName().endsWith(".json"))
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
    public void loadStartingWithTrain(String fileName) {
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


    public Integer getNumTrain() {
        return numTrain;
    }

    public void setNumTrain(Integer numTrain) {
        this.numTrain = numTrain;
    }

    public Train() {
        loadFilenamesStartingWithTrain();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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









    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Train(Long id, M_50592 m_50592) {
        this.id = id;


    }


}
