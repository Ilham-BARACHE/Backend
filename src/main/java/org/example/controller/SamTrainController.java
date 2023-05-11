package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.nio.file.Files;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


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
    private List<Map<String, JsonNode>> lireFichiersJson(String dossier, int startIndex) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, JsonNode>> result = new ArrayList<>();
        File[] fichiers = new File(dossier).listFiles();
        if (fichiers != null) {
            for (File fichier : fichiers) {
                if (fichier.isFile() && fichier.getName().endsWith(".json")) {
                    Map<String, JsonNode> map = new HashMap<>();
                    String nomFichier = fichier.getName();
                    map.put("nomFichier", mapper.valueToTree(nomFichier)); // Ajout de la clé "nomFichier"
                    byte[] contenuFichier = Files.readAllBytes(fichier.toPath());
                    JsonNode jsonContenuFichier = mapper.readTree(contenuFichier);
                    map.put("contenuFichier", jsonContenuFichier); // Ajout du contenu du fichier
                    result.add(map);

                }

            }
            for (int i = 0; i < result.size(); i++) {
                Map<String, JsonNode> map = result.get(i);
                map.put("capteur", mapper.valueToTree("capteur" + (startIndex + i))); // Ajout de la clé "capteur" avec index incrémenté
            }
        }
        return result;
    }



    @GetMapping("/echantillonage")
    public List<Map<String, JsonNode>> getEnveloppes(@RequestParam("site") String site,
                                                     @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                                     @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Time heureTime = Time.valueOf(heure);
        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichierAndHeureFichier(site,dateFichier,heureTime);

        List<Map<String, JsonNode>> capteurs = new ArrayList<>();
        int index = 0; // Initialisation de l'index
        for (Sam sam : sams) {
            String urlsamList = sam.getUrlSam();
            System.out.println(urlsamList);

            List<Map<String, JsonNode>> fichiers = lireFichiersJson(urlsamList, index);
            System.out.println(fichiers);
            capteurs.addAll(fichiers);
            index += fichiers.size(); // Mise à jour de l'index
        }

        return capteurs;
    }






    @GetMapping("/capteurs")
    public List<String> getCapteurs() throws IOException {
        List<String> capteurs = new ArrayList<>();
        Set<String> entetesDejaAjoutes = new HashSet<>(); // ensemble temporaire pour stocker les entêtes déjà ajoutées
        List<M_50592> m50592s = m50592Repository.findAll();
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);
        String outputFolderPath = prop.getProperty("output.folder.path");

        for (M_50592 m50592 : m50592s) {
            File inputFile = new File(outputFolderPath, m50592.getFileName()); // use output folder path as parent directory
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
            JsonNode parametreBENode = rootNode.get("ParametresBE");
            for (int i = 0; i < parametreBENode.size(); i++) {
                JsonNode entete = parametreBENode.get(i).get(0);
                String enteteText = entete.asText();
                if (!entetesDejaAjoutes.contains(enteteText)) { // vérifier si l'entête n'a pas déjà été ajoutée
                if (!(enteteText.contains("D39") || enteteText.contains("D50"))) { // vérifier si l'entête ne commence pas par D39 ou D50
                    capteurs.add(enteteText);
                    entetesDejaAjoutes.add(enteteText); // ajouter l'entête à l'ensemble temporaire
                }
                }
            }
        }

        return capteurs;
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
            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());
            trainMap.put("url", train.getUrl());
            trainMap.put("site",site);

            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                        train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                        train.getDateFichier().equals(sam.getDateFichier()) ) {
                    trainMap.put("heuresam",sam.getHeureFichier());
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
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
                    trainMap.put("statut50592", m50592.getStatut50592());
                    trainMap.put("url50592", m50592.getUrl50592());
                    trainMap.put("heure50592",m50592.getHeureFichier());


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
;

                    trainMap.put("outofband",outofband);
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
                trainMap.put("heuresam",sam.getHeureFichier());
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
                trainMap.put("heure50592",m50592.getHeureFichier());
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


                    trainMap.put("outofband",outofband);




                // create maps to store parameter-status associations
                Map<String, String> statusesBE = new HashMap<>();
                Map<String, String> statusesBL = new HashMap<>();

//loop through parameters and determine their statuses
//                for (int i = 0; i < m50592.getBeR1().getX().size(); i++) {
//                    String parameter = parametreBENode.get(i).get(0).asText();
//                    if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
//                        statusesBE.put(parameter, "NOK");
//                    } else {
//                        statusesBE.put(parameter, "OK");
//                    }
//                }
//                for (int i = 0; i < m50592.getBlR1().getXl().size(); i++) {
//                    String parameter = parametreBLNode.get(i).get(0).asText();
//                    if (m50592.getBlR1().getxFondl().get(i).equals("FF382A") || m50592.getBlR1().getyFondl().get(i).equals("FF382A") || m50592.getBlR1().getzFondl().get(i).equals("FF382A") || m50592.getBlR2().getxFondl2().get(i).equals("FF382A") || m50592.getBlR2().getyFondl2().get(i).equals("FF382A") || m50592.getBlR2().getzFondl2().get(i).equals("FF382A")) {
//                        statusesBL.put(parameter, "NOK");
//                    } else {
//                        statusesBL.put(parameter, "OK");
//                    }
//                }

// add parameter-status maps to train map
                trainMap.put("statusbe", statusesBE);
                trainMap.put("statusbl", statusesBL);

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
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenstatistique(
            @RequestParam("site") String site,
            @RequestParam("typemr") List<String> typemrList,
            @RequestParam(name = "statutsam", required = false) String statutSam,
            @RequestParam(name = "statut50592", required = false) String statut50592 ,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetweenAndStatut50592(site, start, end ,statut50592);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetweenAndStatutSAM(site, start, end,statutSam);


        List<Map<String, Object>> result = new ArrayList<>();
        for (String typemr : typemrList) {
            List<Mr> mrs = mrRepository.findByMr(typemr);
            Map<String, Integer> m505952nokIndexValueMap = new HashMap<>();
            Map<String, Integer> samnokIndexValueMap = new HashMap<>();
            Map<String, Integer> redHeadersCountMap = new HashMap<>();
            Map<Integer, Integer> redHeadersCountSamMap = new HashMap<>();
            List<String> numTrains = new ArrayList<>();
            List<String> Trainssamnok = new ArrayList<>();
            List<String> Trainssamok = new ArrayList<>();
            List<String> Trains50592ok = new ArrayList<>();
            List<String> Trains50592nok = new ArrayList<>();
            int statutbednok =0;
            int statutbehdnok =0;

            int statutbedok =0;
            int statutbehdok =0;
            for (Mr mr : mrs) {
                List<Train> trains = trainRepository.findByNumTrain(mr.getNumTrain());
                for (Train train : trains) {
                    numTrains.add(train.getNumTrain());
                    boolean allSamsOk = true;
                    for (Sam sam : sams) {
                        if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours()
                                && train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes()
                                && train.getDateFichier().equals(sam.getDateFichier())) {

                            if (!sam.getStatutSAM().equals("OK")) {
                                Trainssamnok.add(train.getNumTrain());

                                if (sam.getNbOccultations() != null && sam.getNbOccultations().size() > 0) {
                                    for (int i = 0; i < sam.getNbOccultations().size(); i++) {
                                        if (!sam.getNbOccultations().get(i).equals(sam.getNbEssieux())) {
                                            int index = i;
                                            int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                            redHeadersCountSamMap.put(index, occurrenceCount);
                                        }
                                    }
                                }

                                allSamsOk = false;
                                break;
                            }


                            if (allSamsOk) {


                                    System.out.println("je sus la2");
                                    System.out.println("sam "+sam.getStatutSAM());
                                    Trainssamok.add(train.getNumTrain());


                            }
                            else if(statutSam.equals("uniquement sam")) {

                            }
                        }
                    }
                    boolean all50592Ok = true;


                    for (M_50592 m50592 : m50592s) {
                        if (train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours()
                                && train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes()
                                && train.getDateFichier().equals(m50592.getDateFichier())) {
                            Properties prop = new Properties();
                            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                            prop.load(input);

                            String outputFolderPath = prop.getProperty("output.folder.path");

                            File inputFile = new File(outputFolderPath, m50592.getFileName()); // use output folder path as parent directory
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
                            JsonNode parametreBENode = rootNode.get("ParametresBE");
                            if (!m50592.getStatut50592().equals("OK")) {
                                Trains50592nok.add(train.getNumTrain());

                                    for (int i = 0; i < parametreBENode.size(); i++) {
                                        JsonNode entete = parametreBENode.get(i).get(0);


                                        boolean isRedHeader = false;


                                        if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
                                            isRedHeader = true;
                                            String enteteValue = entete.asText();

                                            // Mise à jour du compteur pour l'en-tête rouge
                                            redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);
                                        }
                                        if (!isRedHeader) {
                                            String enteteValue = entete.asText();
                                            redHeadersCountMap.putIfAbsent(enteteValue, 0);
                                        }



                                    }



                                all50592Ok = false;
                                break;
                            }if (all50592Ok) {
                                Trains50592ok.add(train.getNumTrain());
                                for (int i = 0; i < parametreBENode.size(); i++) {
                                    JsonNode entete = parametreBENode.get(i).get(0);


                                    boolean isRedHeader = false;

                                    if (m50592.getBeR1().getxFond().get(i).equals("00EF2F") || m50592.getBeR1().getyFond().get(i).equals("00EF2F") || m50592.getBeR1().getzFond().get(i).equals("00EF2F") || m50592.getBeR2().getxFond1().get(i).equals("00EF2F") || m50592.getBeR2().getyFond1().get(i).equals("00EF2F") || m50592.getBeR2().getzFond1().get(i).equals("00EF2F")) {
                                        String enteteValue = entete.asText();

                                        // Mise à jour du compteur pour l'en-tête rouge
                                        redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);
                                    }

                                    if (!isRedHeader) {
                                        String enteteValue = entete.asText();
                                        redHeadersCountMap.putIfAbsent(enteteValue, 0);
                                    }
                                }
                            }
                            else if(statut50592.equals("uniquement 50592")) {

                            }
                        }

                        }


                    }



            }
            Map<String, Object> trainMapSam = new HashMap<>();
            Map<String, Object> trainMap50592 = new HashMap<>();
            int count = trainRepository.countBySiteAndDateFichierBetweenAndNumTrainIn(site, start, end, numTrains);

            int countsamok = Trainssamok.size();
            int countsamnok = Trainssamnok.size();
            int count50592ok = Trains50592ok.size();
            int count50592nok = Trains50592nok.size();

            double pourcentagesamok = ((double) countsamok / count) * 100;

            // Affichage des en-têtes rouges et leur nombre de fois 50592 not ok / 50592 ok
            Map<String, Double> percentageMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : redHeadersCountMap.entrySet()) {
                String entete = entry.getKey();
                Integer countbe = entry.getValue();
                System.out.println("Entête: " + entete + ", Nombre de fois: " + countbe);
                m505952nokIndexValueMap.put(entete,countbe);

if(!Trains50592nok.isEmpty()) {
    // Calcul du pourcentage
    double percentagenok = (double) countbe / count50592nok * 100;
    percentageMap.put(entete, percentagenok);
}
                if(!Trains50592ok.isEmpty()) {
                    double percentageok = (double) countbe / count50592ok * 100;
                    percentageMap.put(entete, percentageok);
                }
            }



// sam ok
            if (!Trainssamok.isEmpty()) {
                System.out.println("je suis ici ");
                trainMapSam.put("count", count);
                trainMapSam.put("mr",typemr);
                trainMapSam.put("countsamok", countsamok);


                trainMapSam.put("pourcentage sam ok", pourcentagesamok);
            }


            // Affichage des index et leur nombr de fois sam not ok
            Map<String, Double> percentagesamnokMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : redHeadersCountSamMap.entrySet()) {
                int index = entry.getKey();
                int countbe = entry.getValue();
                System.out.println("Index : " + index + ", Nombre de fois : " + countbe);

                // Ajout à samnokIndexValueMap
                samnokIndexValueMap.put(String.valueOf(index), countbe);

                // Calcul du pourcentage
                double pourentagesam = (double) countbe / countsamnok * 100;
                percentagesamnokMap.put(String.valueOf(index), pourentagesam);
            }
            //sam not ok
            if (!Trainssamnok.isEmpty()) {
                System.out.println("je suis ici ");
                trainMapSam.put("count", count);
                trainMapSam.put("mr",typemr);
                trainMapSam.put("countsamnok", countsamnok);
                trainMapSam.put("counters sam", samnokIndexValueMap);
//                trainMap.put("somme de tous les types mrs", total);
                trainMapSam.put("pourcentage sam nok", percentagesamnokMap);
            }
            //50592 not ok
            if (!Trains50592nok.isEmpty()) {
                trainMap50592.put("count", count);
                trainMap50592.put("mr",typemr);
                trainMap50592.put("count50592NOK", count50592nok);
                trainMap50592.put("counters 50592", m505952nokIndexValueMap);
                trainMap50592.put("percentage50592nok", percentageMap);
            }

            //50592  ok
            if (!Trains50592ok.isEmpty()) {
                trainMap50592.put("count", count);
                trainMap50592.put("mr",typemr);
                trainMap50592.put("count50592OK", count50592ok);
                trainMap50592.put("counters 50592", m505952nokIndexValueMap);
                trainMap50592.put("percentage50592ok", percentageMap);
            }
if (!trainMapSam.isEmpty() ) {

    result.add(trainMapSam);
}
            if (!trainMap50592.isEmpty() ) {
                result.add(trainMap50592);
            }

        }


        Map<String, Object> totalPourcentageMap50592nok = new HashMap<>();
        Map<String, Object> totalPourcentageMap50592ok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamnok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamok = new HashMap<>();
//50592 not ok
        for (Map<String, Object> resultMap50592 : result) {
            if (resultMap50592.containsKey("percentage50592nok")) {
                Map<String, Double> pourcentage50592NOkMap = (Map<String, Double>) resultMap50592.get("percentage50592nok");

                for (Map.Entry<String, Double> entry : pourcentage50592NOkMap.entrySet()) {
                    String capteur = entry.getKey();
                    Double pourcentage = entry.getValue();

                    if (totalPourcentageMap50592nok.containsKey(capteur)) {
                        Double totalPourcentage = (Double) totalPourcentageMap50592nok.get(capteur);
                        totalPourcentageMap50592nok.put(capteur, totalPourcentage + pourcentage);
                    } else {
                        totalPourcentageMap50592nok.put(capteur, pourcentage);
                    }
                }
            }



                if (resultMap50592.containsKey("percentage50592ok")) {
                    Map<String, Double> pourcentage50592OkMap = (Map<String, Double>) resultMap50592.get("percentage50592ok");

                    for (Map.Entry<String, Double> entry : pourcentage50592OkMap.entrySet()) {
                        String capteur = entry.getKey();
                        Double pourcentage = entry.getValue();

                        if (totalPourcentageMap50592ok.containsKey(capteur)) {
                            Double totalPourcentage = (Double) totalPourcentageMap50592ok.get(capteur);
                            totalPourcentageMap50592ok.put(capteur, totalPourcentage + pourcentage);
                        } else {
                            totalPourcentageMap50592ok.put(capteur, pourcentage);
                        }
                    }
                }
            double sommePourcentageSamOk = 0;
            if (resultMap50592.containsKey("pourcentage sam nok")) {
                Map<String, Double> pourcentageSamNOkMap = (Map<String, Double>) resultMap50592.get("pourcentage sam nok");

                for (Map.Entry<String, Double> entry : pourcentageSamNOkMap.entrySet()) {
                    String capteur = entry.getKey();
                    Double pourcentage = entry.getValue();

                    if (totalPourcentageMapSamnok.containsKey(capteur)) {
                        Double totalPourcentage = (Double) totalPourcentageMapSamnok.get(capteur);
                        totalPourcentageMapSamnok.put(capteur, totalPourcentage + pourcentage);
                    } else {
                        totalPourcentageMapSamnok.put(capteur, pourcentage);
                    }
                }
            }


            if (resultMap50592.containsKey("pourcentage sam ok")) {
                Map<String, Double> pourcentageSamOkMap = (Map<String, Double>) resultMap50592.get("pourcentage sam ok");
                for (Double pourcentage : pourcentageSamOkMap.values()) {
                    sommePourcentageSamOk += pourcentage;

                }
                totalPourcentageMapSamok.put("Total pourcentage sam ok", sommePourcentageSamOk);


            }


        }



        result.add(totalPourcentageMap50592nok);
        result.add(totalPourcentageMap50592ok);
        result.add(totalPourcentageMapSamnok);
        result.add(totalPourcentageMapSamok);




        if (result.isEmpty()) {
            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
            return ResponseEntity.noContent().build();
        }
            return ResponseEntity.ok(result);



    }


    @GetMapping("/dataBetweenrMr")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapportmr(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);

        Map<String, Integer> trainCountByMr = new HashMap<>();

        for (Train train : trains) {
            String trainNumber = train.getNumTrain();
            List<Mr> mrs = mrRepository.findAllByNumTrain(trainNumber);

            for (Mr mr : mrs) {
                String mrType = mr.getMr();
                trainCountByMr.put(mrType, trainCountByMr.getOrDefault(mrType, 0) + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : trainCountByMr.entrySet()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("typeMR", entry.getKey());
            resultMap.put("count", entry.getValue());
            result.add(resultMap);
        }

        return ResponseEntity.ok(result);
    }






//
//                    for (Sam sam : sams) {
//                        if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours()
//                                && train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes()
//                                && train.getDateFichier().equals(sam.getDateFichier())) {
//                            List<Integer> nbOccultations = sam.getNbOccultations();
//                            Integer nbEssieux = sam.getNbEssieux();
//                            if (nbOccultations != null && nbOccultations.size() > 0) {
//                                List<Integer> indexes = new ArrayList<>(); // modification
//                                for (int i = 0; i < nbOccultations.size(); i++) { // modification
//                                    if (!nbOccultations.get(i).equals(nbEssieux)) {
//                                        indexes.add(i); // modification
//                                    }
//                                }
//                                if (!indexes.isEmpty()) { // modification
//                                    for (Integer index : indexes) { // modification
//                                        int key = index + 1; // modification
//                                        if (counters.containsKey(key)) {
//                                            Map<String, Integer> map = counters.get(key);
//                                            int count = map.getOrDefault(nbOccultations.get(index).toString(), 0);
//                                            map.put(nbOccultations.get(index).toString(), count + 1);
//                                            indexTotals.put(key, indexTotals.getOrDefault(key, 0) + count + 1); // ajout
//                                        } else {
//                                            Map<String, Integer> map = new HashMap<>();
//                                            map.put(nbOccultations.get(index).toString(), 1);
//                                            counters.put(key, map);
//                                            indexTotals.put(key, indexTotals.getOrDefault(key, 0) + 1); // ajout
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//
//                    if (!counters.isEmpty()) {
//
//                        Map<String, Object> trainMap = new HashMap<>();
//                        trainMap.put("NumTrain", train.getNumTrain());
//                        trainMap.put("Counters", counters);
//                        trainMap.put("IndexTotals", indexTotals); // ajout
//
//                        result.add(trainMap);
//                    }







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