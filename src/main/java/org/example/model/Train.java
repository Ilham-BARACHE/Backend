package org.example.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import javax.persistence.*;
import java.io.File;
import java.sql.Time;
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


    @Column(name = "url")
    private String url;

    @Column(name = "Statut")
    private String Statut;

    @JsonProperty("Num_train")
    @Column(columnDefinition = "Varchar")
    private  String numTrain;

    @Column(name = "site")
    private String site;
    @Column(columnDefinition = "Varchar")
    private String fileName;

    @Column(name = "date_fichier")
    @Temporal(TemporalType.DATE)
    private java.util.Date dateFichier;

    @Column(name = "heure_fichier")
    @Temporal(TemporalType.TIME)
    private java.util.Date heureFichier;

    public Train() {
        loadFilenamesStartingWithTRAIN();
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

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatut() {
        return Statut;
    }

    public void setStatut(String statut) {
        Statut = statut;
    }

    public String getNumTrain() {
        return numTrain;
    }

    public void setNumTrain(String numTrain) {
        this.numTrain = numTrain;
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

    public Train(Long id, String url, String statut, String numTrain, String site, String fileName, Date dateFichier, Time heureFichier) {
        this.id = id;
        this.url = url;
        Statut = statut;
        this.numTrain = numTrain;
        this.site = site;
        this.fileName = fileName;
        this.dateFichier = dateFichier;
        this.heureFichier = heureFichier;
    }


    public void loadFilenamesStartingWithTRAIN() {

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


    public void loadSite(String fileName) {
        String[] tokens = fileName.split("_");
        if (tokens.length >= 2) {
            String name = tokens[0];
            String [] nom = name.split("-");
            String site = nom[1];

            this.setSite(site);
        }
    }



    public void loadStartingWithTRAIN(String fileName) {
        int index = fileName.indexOf("_");
        if (index > 0) { // Vérifier si le nom de fichier contient au moins un "_"
            // Trouver l'index du 2ème "_" en partant de la droite
            int lastIndex = fileName.lastIndexOf("_");
            if (lastIndex > index) {
                String dateTimePart = fileName.substring(index+1, fileName.length()-5); // Extraire la partie qui contient la date et l'heure en excluant l'extension du fichier (.json)



                String[] dateTimeParts = dateTimePart.split("[_ .hms]+");


                if (dateTimeParts.length == 6) { // Vérifier si la partie date-heure a été correctement divisée
                    String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2]; // Concaténer les parties pour former la date
                    String heurePart = dateTimeParts[3] + "h" + dateTimeParts[4] + "m" + dateTimeParts[5]+ "s"; // Concaténer les parties pour former l'heure

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
}
