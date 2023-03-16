package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.example.repository.SamRepository;

import org.w3c.dom.Text;

import javax.persistence.*;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"Temps_ms"})
@Entity
@Data
@Table(name = "SAM005")
public class Sam  {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;


    @JsonProperty("NbEssieux")
    private  Integer NbEssieux;

    @ElementCollection
    @JsonProperty("NbOccultations")
    private List<Integer> NbOccultations;
    @Column(name = "statutSAM")
    private String statutSAM;
    @JsonIgnore
    @Embedded
    private Enveloppes enveloppes;
    @Column(name = "url")
    private String url;

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


    @JsonProperty("Vitesses_1-7_km/h")
    private Double vitesse1_7 ;
    @JsonProperty("Vitesses_2-8_km/h")
    private Double vitesse2_8;
    @JsonProperty("Vitesse_moyenne_km/h")
    private Double vitesse_moy;

    @JsonIgnore
    @Embedded
    private  Temps_ms temps_ms;













    public Integer getNbEssieux() {
        return NbEssieux;
    }



    public List<Integer> getNbOccultations() {
        return NbOccultations;
    }


    public String getStatutSAM() {
        return statutSAM;
    }

    public void setStatutSAM(String statutSAM) {
        this.statutSAM = statutSAM;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    public Sam() {
        loadFilenamesStartingWithSAM();


    }

    public String checkOccultations() {
        if (NbOccultations.stream().allMatch(n -> n.equals(NbEssieux))) {
            statutSAM = "OK";
        } else {
            statutSAM = "NOK";
        }
        return statutSAM;

    }

    public void loadFilenamesStartingWithSAM() {

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






    public void loadSite(String fileName) {
        String[] tokens = fileName.split("_");
        if (tokens.length >= 2) {
            String name = tokens[0];
            String [] nom = name.split("-");
            String site = nom[1];

            this.setSite(site);
        }
    }



    public void loadStartingWithSam(String fileName) {
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





    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sam(Long id, Integer nbEssieux, List<Integer> nbOccultations, String statutSAM, String url, String site, String fileName, Date dateFichier, Date heureFichier, Double vitesse1_7, Double vitesse2_8, Double vitesse_moy) {
        this.id = id;
        NbEssieux = nbEssieux;
        NbOccultations = nbOccultations;
        this.statutSAM = statutSAM;
        this.url = url;
        this.site = site;
        this.fileName = fileName;
        this.dateFichier = dateFichier;
        this.heureFichier = heureFichier;
        this.vitesse1_7 = vitesse1_7;
        this.vitesse2_8 = vitesse2_8;
        this.vitesse_moy = vitesse_moy;
    }
}
