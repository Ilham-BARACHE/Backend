package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.example.component.Utils;
import org.example.model.M_50592;
import org.example.model.Mr;
import org.example.model.Sam;
import org.example.model.Train;
import org.example.repository.M_50592Repository;
import org.example.repository.MrRepository;
import org.example.repository.SamRepository;
import org.example.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


@RestController

@CrossOrigin("http://localhost:3000/")
public class SamTrainController {


    @Autowired
    private Utils utils;


    private final SamRepository samRepository;


    private final TrainRepository trainRepository;

    private final MrRepository mrRepository;

    private final M_50592Repository m50592Repository;


    public SamTrainController(SamRepository samRepository, TrainRepository trainRepository, MrRepository mrRepository, M_50592Repository m50592Repository) {
        this.samRepository = samRepository;
        this.trainRepository = trainRepository;
        this.mrRepository = mrRepository;
        this.m50592Repository = m50592Repository;

    }










    private File getFileBySiteAndDateFichier(String site, Date dateFichier, Time heure) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        System.out.println( heure);
        String dateFichierStr = new SimpleDateFormat("yyyy.MM.dd").format(dateFichier);
        String heureStr = new SimpleDateFormat("HH'h'mm'm'ss's'").format(new Date(heure.getTime()));
        System.out.println("SAM005-" + site + "_" + dateFichierStr + "_" + heureStr + ".json");
        File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005-" + site + "_" + dateFichierStr + "_" + heureStr) && name.endsWith(".json"));

        System.out.println(samFiles.length == 0 ? "Le tableau est vide" : "Le tableau contient des éléments");


        if (samFiles != null && samFiles.length > 0) {
            System.out.println(samFiles[0]);
            return samFiles[0];


        } else {
            return null;
        }
    }


    @GetMapping("/temps")
    public List<Map<String, JsonNode>> getTempsMs(@RequestParam("site") String site,
                                                  @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                  @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        File file = getFileBySiteAndDateFichier(site, dateFichier, heureTime);
        List<Map<String, JsonNode>> tempsMsNodesList = new ArrayList<>();

        if (file != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);
            JsonNode tempsMsNodes = rootNode.get("Temps_ms");
            JsonNode t1Nodes = tempsMsNodes.get("t1");
            JsonNode t2Nodes = tempsMsNodes.get("t2");
            JsonNode t3Nodes = tempsMsNodes.get("t3");

            // Vérifier que les trois tableaux ont la même longueur
            if (t1Nodes.size() == t2Nodes.size() && t2Nodes.size() == t3Nodes.size()) {
                for (int i = 0; i < t1Nodes.size(); i++) {
                    Map<String, JsonNode> tempsMsMap = new HashMap<>();
                    tempsMsMap.put("t1", t1Nodes.get(i));
                    tempsMsMap.put("t2", t2Nodes.get(i));
                    tempsMsMap.put("t3", t3Nodes.get(i));
                    tempsMsNodesList.add(tempsMsMap);
                }
            }
        }

        return tempsMsNodesList;
    }

    //Api pour les urls des images
    @GetMapping("/urls")
    public ResponseEntity<List<Map<String, Object>>> geturl(@RequestParam("site") String site,
                                                  @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                  @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Sam> sams = samRepository.findBySiteAndDateFichierAndHeureFichier(site,dateFichier,heureTime);
        List<Train> trains = trainRepository.findBySiteAndDateFichierAndHeureFichier(site,dateFichier,heureTime);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierAndHeureFichier(site,dateFichier,heureTime);


        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();

            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());

            // Créer une liste de noms d'images PNG à partir de l'URL
            List<Map<String, Object>> imagestrain = new ArrayList<>();
            String url = train.getUrl() + '/';
            int index = url.lastIndexOf('/');
            String directory = url.substring(0, index + 1);
            File folder = new File(directory);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                        Map<String, Object> image = new HashMap<>();
                        image.put("name", file.getName());

                        try {
                            byte[] fileContent = FileUtils.readFileToByteArray(file);
                            String base64 = Base64.getEncoder().encodeToString(fileContent);
                            image.put("content", base64);
                            imagestrain.add(image);
                        } catch (IOException e) {
                            // handle exception
                        }
                    }
                }
            }

            trainMap.put("images", imagestrain);
            trainMap.put("url", train.getUrl());

            // Créer une liste de noms d'images PNG à partir de l'URL
//            List<String> images = new ArrayList<>();
//            String url = train.getUrl();
//            int index = url.lastIndexOf('/');
//            String directory = url.substring(0, index + 1);
//            File folder = new File(directory);
//            File[] files = folder.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
//                        images.add(file.getName());
//                    }
//                }
//            }
//
//
//            trainMap.put("images", images);


            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(sam.getDateFichier()) ) {

                    // Créer une liste de noms d'images PNG à partir de l'URL
                    List<Map<String, Object>> images = new ArrayList<>();
                    String urlsam = sam.getUrlSam() + '/';
                    int indexsam = urlsam.lastIndexOf('/');
                    String directorysam = urlsam.substring(0, indexsam + 1);
                    File foldersam = new File(directorysam);
                    File[] filesSam = foldersam.listFiles();
                    if (filesSam != null) {
                        for (File file : filesSam) {
                            if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                                Map<String, Object> image = new HashMap<>();
                                image.put("name", file.getName());

                                try {
                                    byte[] fileContent = FileUtils.readFileToByteArray(file);
                                    String base64 = Base64.getEncoder().encodeToString(fileContent);
                                    image.put("content", base64);
                                    images.add(image);
                                } catch (IOException e) {
                                    // handle exception
                                }
                            }
                        }
                    }

                    trainMap.put("imagesSam", images);
                    trainMap.put("urlSam", sam.getUrlSam());


                    foundSam = true;
                    break;
                }
            }

            for (M_50592 m50592 : m50592s) {
                if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(m50592.getDateFichier())) {

                    // Créer une liste de noms d'images PNG à partir de l'URL
                    List<Map<String, Object>> images50592 = new ArrayList<>();
                    String url50592 = m50592.getUrl50592() + '/';
                    int index50592 = url50592.lastIndexOf('/');
                    String directory50592 = url50592.substring(0, index50592 + 1);
                    File folder50592 = new File(directory50592);
                    File[] files50592 = folder50592.listFiles();
                    if (files50592 != null) {
                        for (File file : files50592) {
                            if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                                Map<String, Object> image = new HashMap<>();
                                image.put("name", file.getName());

                                try {
                                    byte[] fileContent = FileUtils.readFileToByteArray(file);
                                    String base64 = Base64.getEncoder().encodeToString(fileContent);
                                    image.put("content", base64);
                                    images50592.add(image);
                                } catch (IOException e) {
                                    // handle exception
                                }
                            }
                        }
                    }

                    trainMap.put("images50592", images50592);
                    trainMap.put("url50592", m50592.getUrl50592());





                    found50592 = true;
                    break;


                }
            }

            if (!foundSam) {

                trainMap.put("urlSam", null);

            }

            if (!found50592) {

                trainMap.put("url50592", null);

            }

            result.add(trainMap);
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }









    //Api pour la partie Jour J
    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);


        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();
            trainMap.put("numTrain", train.getNumTrain());
//            trainMap.put("dateFichier", train.getDateFichier());
//            trainMap.put("heureFichier", train.getHeureFichier());
//            trainMap.put("url", train.getUrl());
//            trainMap.put("site",site);

            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(sam.getDateFichier()) ) {
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                    trainMap.put("NbEssieux", sam.getNbEssieux());
//                    trainMap.put("urlSam", sam.getUrlSam());
//                    trainMap.put("statutSAM", sam.getStatutSAM());
//                    trainMap.put("NbOccultations", sam.getNbOccultations());

                    foundSam = true;
                    break;
                }
            }

            for (M_50592 m50592 : m50592s) {
                if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(m50592.getDateFichier())) {
//                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                    trainMap.put("statut50592", m50592.getStatut50592());
//                    trainMap.put("url50592", m50592.getUrl50592());
//
//
//                    trainMap.put("ber1",m50592.getBeR1());
//                    trainMap.put("ber2" ,m50592.getBeR2());
//                    trainMap.put("blr1" ,m50592.getBlR1() );
//                    trainMap.put("blr2" ,m50592.getBlR2());



                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);

                        String outputFolderPath = prop.getProperty("output.folder.path");

                        File inputFile = new File(outputFolderPath, m50592.getFileName()); // use output folder path as parent directory
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
                        JsonNode outofband = rootNode.get("OutOfBand");
                    Map<String, Object> entetesbl = new HashMap<>();

                    Map<String, Object> frequencesbl = new HashMap<>();

                    List<Object> entetesbe = new ArrayList<>();

                    List<Object> frequencesbe = new ArrayList<>();
                    System.out.println("size de mes bl "+parametreBLNode.size());
                    for (int i=0 ;i <parametreBLNode.size() ;i++){
                        JsonNode entete = parametreBLNode.get(i).get(0);
                        JsonNode frequence = parametreBLNode.get(i).get(1);
                        entetesbl.put("entete" + i, entete);
                        frequencesbl.put("frequence" + i, frequence);
                    }

                    for (int i=0 ;i <parametreBENode.size() ;i++){
                        JsonNode entete = parametreBENode.get(i).get(0);
                        JsonNode frequence = parametreBENode.get(i).get(1);
                        entetesbe.add( entete);
                        frequencesbe.add( frequence);
                    }


                    trainMap.put("entetesbl",entetesbl);
                        trainMap.put("frequencebl",entetesbl);

                    trainMap.put("entetes",entetesbe);
                    trainMap.put("frequence",frequencesbe);
//                        trainMap.put("parbE",parametreBENode);
//
//                    trainMap.put("outofband",outofband);
                        found50592 = true;
                        break;


                }
            }

            if (!foundSam) {
                trainMap.put("vitesse_moy", null);
                trainMap.put("NbEssieux", null);
                trainMap.put("urlSam", null);
                trainMap.put("statutSAM", null);
                trainMap.put("NbOccultations", null);
                trainMap.put("tempsMs", null);
            }

            if (!found50592) {
                trainMap.put("meteo", null);
                trainMap.put("statut50592",null);
                trainMap.put("url50592", null);
                trainMap.put("BE_R1",null);
                trainMap.put("BE_R2",null);
                trainMap.put("BL_R1",null);
                trainMap.put("BL_R2",null);
            }

            Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
            if (mr != null) {
                trainMap.put("mr", mr.getMr());
            }
            result.add(trainMap);
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }







//Api pour la partie historique

    @GetMapping("/dataBetween")
public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetween(
        @RequestParam("site") String site,
        @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) throws Exception{


    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
    List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
    List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

    List<Map<String, Object>> result = new ArrayList<>();
    for (Train train : trains) {
        Map<String, Object> trainMap = new HashMap<>();
        trainMap.put("numTrain", train.getNumTrain());
        trainMap.put("dateFichier", train.getDateFichier());
        trainMap.put("heureFichier", train.getHeureFichier());

        trainMap.put("site",site);

        boolean foundSam = false;
        boolean found50592 = false;

        for (Sam sam : sams) {
            if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                    train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                    train.getDateFichier().equals(sam.getDateFichier()) ) {
                trainMap.put("vitesse_moy", sam.getVitesse_moy());
                trainMap.put("datesam",sam.getDateFichier());
                trainMap.put("NbEssieux", sam.getNbEssieux());
                trainMap.put("urlSam", sam.getUrlSam());
                trainMap.put("statutSAM", sam.getStatutSAM());
                trainMap.put("NbOccultations", sam.getNbOccultations());



                foundSam = true;
                break;
            }
        }

        for (M_50592 m50592 : m50592s) {
            if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                    train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                    train.getDateFichier().equals(m50592.getDateFichier())) {
                trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                trainMap.put("date50592",m50592.getDateFichier());
                trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
                trainMap.put("statut50592", m50592.getStatut50592());
                trainMap.put("url50592", m50592.getUrl50592());
                trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());


                trainMap.put("ber1",m50592.getBeR1());
                trainMap.put("ber2" ,m50592.getBeR2());
                trainMap.put("blr1" ,m50592.getBlR1() );
                trainMap.put("blr2" ,m50592.getBlR2());





                Properties prop = new Properties();
                InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                prop.load(input);

                String outputFolderPath = prop.getProperty("output.folder.path");

                File inputFile = new File(outputFolderPath, m50592.getFileName()); // use output folder path as parent directory
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                JsonNode parametreBENode = rootNode.get("ParametresBE");
                JsonNode parametreBLNode = rootNode.get("ParametresBL");
                JsonNode outbande = rootNode.get("OutOfBand");

trainMap.put("outofband",outbande);
                trainMap.put("parbL",parametreBLNode);

                trainMap.put("parbE",parametreBENode);


                found50592 = true;
                break;
            }
        }

        if (!foundSam) {
            trainMap.put("vitesse_moy", null);
            trainMap.put("NbEssieux", null);
            trainMap.put("urlSam", null);
            trainMap.put("statutSAM", null);
            trainMap.put("NbOccultations", null);
            trainMap.put("tempsMs", null);
        }

        if (!found50592) {
            trainMap.put("meteo", null);
            trainMap.put("statut50592",null);
            trainMap.put("url50592", null);
            trainMap.put("BE_R1",null);
            trainMap.put("BE_R2",null);
            trainMap.put("BL_R1",null);
            trainMap.put("BL_R2",null);
        }

        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
        if (mr != null) {
            trainMap.put("mr", mr.getMr());
        }
        result.add(trainMap);
    }

    if (result.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(result);
}

//Api pour la partie rapport à la demande
    @GetMapping("/dataBetweenDaterapport")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAMBetween(
            @RequestParam("site") String site,
            @RequestParam(name = "statutsam", required = false) String statutSam,
            @RequestParam(name = "statut50592", required = false) String statut50592 ,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);



        List<Map<String, Object>> result = new ArrayList<>();

        // Vérifier si les deux statuts sont sélectionnés
        if (statutSam != null && statut50592 != null) {
            // Récupérer les données correspondantes pour les deux statuts
            List<Sam> sams = samRepository.findBySiteAndStatutSAMAndDateFichierBetween(site, statutSam, start, end);
            List<M_50592> m_50592s = m50592Repository.findBySiteAndStatut50592AndDateFichierBetween(site, statut50592, start, end);

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam sam : sams) {
                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(sam.getDateFichier()) ) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                        trainMap.put("datesam",sam.getDateFichier());
//                        trainMap.put("NbEssieux", sam.getNbEssieux());
//                        trainMap.put("urlSam", sam.getUrlSam());
//                        trainMap.put("statutSAM", sam.getStatutSAM());
//                        trainMap.put("NbOccultations", sam.getNbOccultations());
                        hasSam = true;
                    }
                }
                for (M_50592 m50592 : m_50592s) {
                    if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
//                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                        trainMap.put("date50592",m50592.getDateFichier());
//                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
//                        trainMap.put("statut50592", m50592.getStatut50592());
//                        trainMap.put("url50592", m50592.getUrl50592());
//                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592.getBlR1().getxFondl().get(i).equals("FF382A") || m50592.getBlR1().getyFondl().get(i).equals("FF382A") || m50592.getBlR1().getzFondl().get(i).equals("FF382A") || m50592.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam && has50592) {
                    result.add(trainMap);
                }
            }
        }else if (statutSam != null || statut50592 != null) {

            // Au moins un statut a été sélectionné, récupérer les données correspondantes
            List<Sam> samsuniquement = new ArrayList<>();
            List<M_50592> m_50592suniquement = new ArrayList<>();
            if (statutSam != null) {
                samsuniquement = samRepository.findBySiteAndDateFichierBetween(site, start, end);
            }
            if (statut50592 != null) {
                m_50592suniquement = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
            }

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam samuniquement : samsuniquement) {
                    if (statutSam.equals("uniquement sam") && train.getHeureFichier().getHours() == samuniquement.getHeureFichier().getHours() && train.getHeureFichier().getMinutes() == samuniquement.getHeureFichier().getMinutes() && train.getDateFichier().equals(samuniquement.getDateFichier())) {
                        trainMap.put("vitesse_moy", samuniquement.getVitesse_moy());
//                        trainMap.put("datesam",samuniquement.getDateFichier());
//                        trainMap.put("NbEssieux", samuniquement.getNbEssieux());
//                        trainMap.put("urlSam", samuniquement.getUrlSam());
//                        trainMap.put("statutSAM", samuniquement.getStatutSAM());
//                        trainMap.put("NbOccultations", samuniquement.getNbOccultations());
                        hasSam = true;
                    }
                }

                for (M_50592 m50592uniquement : m_50592suniquement) {
                    if (statut50592.equals("uniquement 50592") && train.getHeureFichier().getHours() == m50592uniquement.getHeureFichier().getHours() && train.getHeureFichier().getMinutes() == m50592uniquement.getHeureFichier().getMinutes() && train.getDateFichier().equals(m50592uniquement.getDateFichier())) {
//                        trainMap.put("meteo", m50592uniquement.getEnvironnement().getMeteo());
//                        trainMap.put("date50592",m50592uniquement.getDateFichier());
//                        trainMap.put("compteur",m50592uniquement.getEnvironnement().getCompteurEssieuxSortie());
//                        trainMap.put("statut50592", m50592uniquement.getStatut50592());
//                        trainMap.put("url50592", m50592uniquement.getUrl50592());
//                        trainMap.put("compteur",m50592uniquement.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592uniquement.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592uniquement.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592uniquement.getBeR1().getxFond().get(i).equals("FF382A") || m50592uniquement.getBeR1().getyFond().get(i).equals("FF382A") || m50592uniquement.getBeR1().getzFond().get(i).equals("FF382A") || m50592uniquement.getBeR2().getxFond1().get(i).equals("FF382A") || m50592uniquement.getBeR2().getyFond1().get(i).equals("FF382A") || m50592uniquement.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592uniquement.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592uniquement.getBlR1().getxFondl().get(i).equals("FF382A") || m50592uniquement.getBlR1().getyFondl().get(i).equals("FF382A") || m50592uniquement.getBlR1().getzFondl().get(i).equals("FF382A") || m50592uniquement.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592uniquement.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592uniquement.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam || has50592) {
                    result.add(trainMap);
                }
            }

            // Au moins un statut a été sélectionné, récupérer les données correspondantes
            List<Sam> sams = new ArrayList<>();
            List<M_50592> m_50592s = new ArrayList<>();
            if (statutSam != null) {
                sams = samRepository.findBySiteAndStatutSAMAndDateFichierBetween(site, statutSam, start, end);
            }
            if (statut50592 != null) {
                m_50592s = m50592Repository.findBySiteAndStatut50592AndDateFichierBetween(site, statut50592, start, end);
            }

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam sam : sams) {
                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(sam.getDateFichier()) ) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                        trainMap.put("datesam",sam.getDateFichier());
//                        trainMap.put("NbEssieux", sam.getNbEssieux());
//                        trainMap.put("urlSam", sam.getUrlSam());
//                        trainMap.put("statutSAM", sam.getStatutSAM());
//                        trainMap.put("NbOccultations", sam.getNbOccultations());
                        hasSam = true;
                    }
                }

                for (M_50592 m50592 : m_50592s) {
                    if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
//                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                        trainMap.put("date50592",m50592.getDateFichier());
//                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
//                        trainMap.put("statut50592", m50592.getStatut50592());
//                        trainMap.put("url50592", m50592.getUrl50592());
//                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592.getBlR1().getxFondl().get(i).equals("FF382A") || m50592.getBlR1().getyFondl().get(i).equals("FF382A") || m50592.getBlR1().getzFondl().get(i).equals("FF382A") || m50592.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam || has50592) {
                    result.add(trainMap);
                }
            }
        }
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }




//Api pour la partie rapoort automatique

    @GetMapping("/dataBetweenrapport")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapport(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> synthese = new HashMap<>();


        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();
            Map<String, Object> SAMNOK = new HashMap<>();
            Map<String, Object> M50592NOK = new HashMap<>();
            Map<String, Object> synthesesam = new HashMap<>();
            Map<String, Object> synthese50592 = new HashMap<>();
            trainMap.put("numTrain", train.getNumTrain());
            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());

            trainMap.put("site",site);

            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(sam.getDateFichier()) ) {
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    trainMap.put("datesam",sam.getDateFichier());
                    trainMap.put("NbEssieux", sam.getNbEssieux());
                    trainMap.put("urlSam", sam.getUrlSam());
                    trainMap.put("statutSAM", sam.getStatutSAM());
                    trainMap.put("NbOccultations", sam.getNbOccultations());
if (sam.getStatutSAM().equals("NOK")){
    SAMNOK.put("numTrain", train.getNumTrain());
    SAMNOK.put("dateFichier", train.getDateFichier());
    SAMNOK.put("heureFichier", train.getHeureFichier());
    SAMNOK.put("vitesse_moy", sam.getVitesse_moy());
    SAMNOK.put("datesam",sam.getDateFichier());
    SAMNOK.put("NbEssieux", sam.getNbEssieux());
    SAMNOK.put("urlSam", sam.getUrlSam());
    SAMNOK.put("statutSAM", sam.getStatutSAM());
    SAMNOK.put("NbOccultations", sam.getNbOccultations());

    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
    if (mr != null) {
        SAMNOK.put("mr", mr.getMr());
    }
}


                    foundSam = true;
                    break;
                }
            }

            for (M_50592 m50592 : m50592s) {
                if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(m50592.getDateFichier())) {
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("date50592",m50592.getDateFichier());
                    trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
                    trainMap.put("statut50592", m50592.getStatut50592());
                    trainMap.put("url50592", m50592.getUrl50592());
                    trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());









                    if (m50592.getStatut50592().equals("NOK")){
                        M50592NOK.put("numTrain", train.getNumTrain());
                        M50592NOK.put("dateFichier", train.getDateFichier());
                        M50592NOK.put("heureFichier", train.getHeureFichier());
                        M50592NOK.put("meteo", m50592.getEnvironnement().getMeteo());
                        M50592NOK.put("date50592",m50592.getDateFichier());
                        M50592NOK.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
                        M50592NOK.put("statut50592", m50592.getStatut50592());
                        M50592NOK.put("url50592", m50592.getUrl50592());
                        M50592NOK.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());
                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                        if (mr != null) {
                            M50592NOK.put("mr", mr.getMr());
                        }
                    }

                    found50592 = true;
                    break;



                }
            }

            if (!foundSam) {
                trainMap.put("vitesse_moy", null);
                trainMap.put("NbEssieux", null);
                trainMap.put("urlSam", null);
                trainMap.put("statutSAM", null);
                trainMap.put("NbOccultations", null);
                trainMap.put("tempsMs", null);
            }

            if (!found50592) {
                trainMap.put("meteo", null);
                trainMap.put("statut50592",null);
                trainMap.put("url50592", null);
                trainMap.put("BE_R1",null);
                trainMap.put("BE_R2",null);
                trainMap.put("BL_R1",null);
                trainMap.put("BL_R2",null);
            }

            Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
            if (mr != null) {
                trainMap.put("mr", mr.getMr());
            }

            if(!trainMap.isEmpty())
            {
            synthese.put("Synthese",trainMap);

    result.add( synthese); // add any additional entries to synthese map if needed
}




            if (!SAMNOK.isEmpty()) { // Vérification si la Map n'est pas vide
synthesesam.put("SAMNOK" ,SAMNOK);

                result.add(synthesesam);
            }

            if (!M50592NOK.isEmpty()) { // Vérification si la Map n'est pas vide
            synthese50592.put("50592NOK",M50592NOK);

                result.add(synthese50592);
            }


        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok(result);
    }




//Api pour statistique

    @GetMapping("/dataBetweenstatistique")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAMBetweenstatistique(
            @RequestParam("site") String site,

            @RequestParam(name = "statutsam", required = false) String statutSam,
            @RequestParam(name = "statut50592", required = false) String statut50592 ,
            @RequestParam("typemr") String typemr,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Mr> mrs = mrRepository.findByMr(typemr);



        List<Map<String, Object>> result = new ArrayList<>();

        // Vérifier si les deux statuts sont sélectionnés
        if (statutSam != null && statut50592 != null) {
            // Récupérer les données correspondantes pour les deux statuts
            List<Sam> sams = samRepository.findBySiteAndStatutSAMAndDateFichierBetween(site, statutSam, start, end);
            List<M_50592> m_50592s = m50592Repository.findBySiteAndStatut50592AndDateFichierBetween(site, statut50592, start, end);

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam sam : sams) {
                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(sam.getDateFichier()) ) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("datesam",sam.getDateFichier());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("urlSam", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());
                        hasSam = true;
                    }
                }
                for (M_50592 m50592 : m_50592s) {
                    if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("date50592",m50592.getDateFichier());
                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592.getBlR1().getxFondl().get(i).equals("FF382A") || m50592.getBlR1().getyFondl().get(i).equals("FF382A") || m50592.getBlR1().getzFondl().get(i).equals("FF382A") || m50592.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam && has50592) {
                    result.add(trainMap);
                }
            }
        }else if (statutSam != null || statut50592 != null) {

            // Au moins un statut a été sélectionné, récupérer les données correspondantes
            List<Sam> samsuniquement = new ArrayList<>();
            List<M_50592> m_50592suniquement = new ArrayList<>();
            if (statutSam != null) {
                samsuniquement = samRepository.findBySiteAndDateFichierBetween(site, start, end);
            }
            if (statut50592 != null) {
                m_50592suniquement = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
            }

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam samuniquement : samsuniquement) {
                    if (statutSam.equals("uniquement sam") && train.getHeureFichier().getHours() == samuniquement.getHeureFichier().getHours() && train.getHeureFichier().getMinutes() == samuniquement.getHeureFichier().getMinutes() && train.getDateFichier().equals(samuniquement.getDateFichier())) {
                        trainMap.put("vitesse_moy", samuniquement.getVitesse_moy());
                        trainMap.put("datesam",samuniquement.getDateFichier());
                        trainMap.put("NbEssieux", samuniquement.getNbEssieux());
                        trainMap.put("urlSam", samuniquement.getUrlSam());
                        trainMap.put("statutSAM", samuniquement.getStatutSAM());
                        trainMap.put("NbOccultations", samuniquement.getNbOccultations());
                        hasSam = true;
                    }
                }

                for (M_50592 m50592uniquement : m_50592suniquement) {
                    if (statut50592.equals("uniquement 50592") && train.getHeureFichier().getHours() == m50592uniquement.getHeureFichier().getHours() && train.getHeureFichier().getMinutes() == m50592uniquement.getHeureFichier().getMinutes() && train.getDateFichier().equals(m50592uniquement.getDateFichier())) {
                        trainMap.put("meteo", m50592uniquement.getEnvironnement().getMeteo());
                        trainMap.put("date50592",m50592uniquement.getDateFichier());
                        trainMap.put("compteur",m50592uniquement.getEnvironnement().getCompteurEssieuxSortie());
                        trainMap.put("statut50592", m50592uniquement.getStatut50592());
                        trainMap.put("url50592", m50592uniquement.getUrl50592());
                        trainMap.put("compteur",m50592uniquement.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592uniquement.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592uniquement.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592uniquement.getBeR1().getxFond().get(i).equals("FF382A") || m50592uniquement.getBeR1().getyFond().get(i).equals("FF382A") || m50592uniquement.getBeR1().getzFond().get(i).equals("FF382A") || m50592uniquement.getBeR2().getxFond1().get(i).equals("FF382A") || m50592uniquement.getBeR2().getyFond1().get(i).equals("FF382A") || m50592uniquement.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592uniquement.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592uniquement.getBlR1().getxFondl().get(i).equals("FF382A") || m50592uniquement.getBlR1().getyFondl().get(i).equals("FF382A") || m50592uniquement.getBlR1().getzFondl().get(i).equals("FF382A") || m50592uniquement.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592uniquement.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592uniquement.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam || has50592) {
                    result.add(trainMap);
                }
            }

            // Au moins un statut a été sélectionné, récupérer les données correspondantes
            List<Sam> sams = new ArrayList<>();
            List<M_50592> m_50592s = new ArrayList<>();
            if (statutSam != null) {
                sams = samRepository.findBySiteAndStatutSAMAndDateFichierBetween(site, statutSam, start, end);
            }
            if (statut50592 != null) {
                m_50592s = m50592Repository.findBySiteAndStatut50592AndDateFichierBetween(site, statut50592, start, end);
            }

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("site", site);

                boolean hasSam = false, has50592 = false;
                for (Sam sam : sams) {
                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(sam.getDateFichier()) ) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("datesam",sam.getDateFichier());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("urlSam", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());
                        hasSam = true;
                    }
                }

                for (M_50592 m50592 : m_50592s) {
                    if ( train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("date50592",m50592.getDateFichier());
                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());

                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);
                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592.getFileName());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
// create maps to store parameter-status associations
                        Map<String, String> statusesBE = new HashMap<>();
                        Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
                        for (int i = 0; i < m50592.getBeR1().getX().size(); i++) {
                            String parameter = parametreBLNode.get(0).get(i).asText();
                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                statusesBE.put(parameter, "NOK");
                            } else {
                                statusesBE.put(parameter, "OK");
                            }
                        }
                        for (int i = 0; i < m50592.getBlR1().getXl().size(); i++) {
                            String parameter = parametreBENode.get(0).get(i).asText();
                            if (m50592.getBlR1().getxFondl().get(i).equals("FF382A") || m50592.getBlR1().getyFondl().get(i).equals("FF382A") || m50592.getBlR1().getzFondl().get(i).equals("FF382A") || m50592.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592.getBlR2().getzFondl2().get(i).equals("FF382A")) {
                                statusesBL.put(parameter, "NOK");
                            } else {
                                statusesBL.put(parameter, "OK");
                            }
                        }

// add parameter-status maps to train map
                        trainMap.put("statusbe", statusesBE);
                        trainMap.put("statusbl", statusesBL);
                        has50592 = true;
                    }
                }

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                if (hasSam || has50592) {
                    result.add(trainMap);
                }
            }
        }
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

















//    private int lastMonthOfQuarter(int month) {
//        if (month <= 0 || month > 12) {
//            throw new IllegalArgumentException("Invalid month: " + month);
//        }
//        int quarter = (month - 1) / 3 + 1; // calcul du numéro de trimestre
//        return quarter * 3; // le dernier mois du trimestre est le mois numéro 3, 6, 9 ou 12
//    }
//
//    @GetMapping("/dataQuarterlySAM")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndQuarterSAM(
//
//    ) throws Exception {
//
//        LocalDate startDate = LocalDate.of(2023, 1, 1);
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        while (true) {
//            LocalDate endDate = startDate.plusMonths(3);
//            if (endDate.getYear() != startDate.getYear()) {
//                endDate = endDate.withYear(startDate.getYear());
//            }
//            System.out.println("Trimestre " + ((endDate.getMonthValue() + 2) / 3) + " " + startDate + " - " + endDate.minusDays(1));
//
//
//            int lastMonth = lastMonthOfQuarter(endDate.getMonthValue()); // on utilise endDate au lieu de startDate
//            LocalDate endQuarterDate = LocalDate.of(endDate.getYear(), lastMonth, 30).with(TemporalAdjusters.lastDayOfMonth()); // on calcule la fin du trimestre
//            System.out.println("Fin de trimestre: " + endQuarterDate);
//
//
//            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//            Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//            List<Train> trains = trainRepository.findAll();
//
//            List<Sam> sams = samRepository.findAll();
//            List<M_50592> m_50592s = m50592Repository.findAll();
////            List<List<JsonNode>> tempsMsNodesList = getTempsMsBetween(site, startDate, endDate);
//
//            for (Sam sam : sams) {
//for(M_50592 m50592 : m_50592s){
//                Map<String, Object> trainMap = null;
//                for (Train train : trains) {
//                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
//                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
//                            train.getDateFichier().equals(sam.getDateFichier()) && train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
//                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
//                            train.getDateFichier().equals(m50592.getDateFichier())) {
//                        trainMap = new HashMap<>();
//                        trainMap.put("numTrain", train.getNumTrain());
//                        trainMap.put("dateFichier", train.getDateFichier());
//                        trainMap.put("heureFichier", train.getHeureFichier());
////                        trainMap.put("url", train.getUrl());
//                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                        trainMap.put("datesam", sam.getDateFichier());
////                        List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
////                        trainMap.put("tempsMs", tempsList);
//
//                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
//                        if (mr != null) {
//                            trainMap.put("mr", mr.getMr());
//                        }
//                        result.add(trainMap);
//                        break; // On a trouvé le train correspondant à ce SAM, on passe au prochain SAM
//                    }
//                }
//            }
//        }
//
//            if (endDate.isAfter(LocalDate.now())) {
//                break;
//            }
//            startDate = endDate;
//        }
//
//        if (result.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(result);
//    }





//    @GetMapping("/dataBetweenDatesamNOK")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierNOK(
//            @RequestParam("site") String site,
//            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
//        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);
//        List<M_50592> m_50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
//
//        if (sams.isEmpty() && trains.isEmpty() && m_50592s.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Sam sam : sams) {
//            if (sam.getStatutSAM().equals("NOK")) {
//                for (Train train : trains) {
//                    if (train.getDateFichier().equals(sam.getDateFichier()) &&
//                            train.getHeureFichier().equals(sam.getHeureFichier())) {
//                        Map<String, Object> trainMap = new HashMap<>();
//                        trainMap.put("id", train.getId());
//                        trainMap.put("numTrain", train.getNumTrain());
//                        trainMap.put("dateFichier", train.getDateFichier());
//                        trainMap.put("heureFichier", train.getHeureFichier());
//                        trainMap.put("url", train.getStatut());
//
//
//
//                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                        trainMap.put("id", sam.getId());
//                        trainMap.put("NbEssieux", sam.getNbEssieux());
//                        trainMap.put("url", sam.getUrlSam());
//                        trainMap.put("statutSAM", sam.getStatutSAM());
//                        trainMap.put("NbOccultations", sam.getNbOccultations());
//
//                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
//                        if (mr != null) {
//                            trainMap.put("mr", mr.getMr());
//                        }
//
//                        result.add(trainMap);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return ResponseEntity.ok(result);
//    }

}