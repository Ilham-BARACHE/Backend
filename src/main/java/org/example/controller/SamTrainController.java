package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.component.Utils;

import org.apache.commons.io.FileUtils;
import org.example.model.*;
import org.example.repository.*;

import org.example.model.*;
import org.example.repository.*;
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


@RestController
public class SamTrainController {


    @Autowired
    private Utils utils;


    private final SamRepository samRepository;


    private final TrainRepository trainRepository;

    private final MrRepository mrRepository;

    private final M_50592Repository m50592Repository;

    private  final ResultRepository resultRepository;


    public SamTrainController(SamRepository samRepository, TrainRepository trainRepository, MrRepository mrRepository, M_50592Repository m50592Repository,ResultRepository resultRepository) {
        this.samRepository = samRepository;
        this.trainRepository = trainRepository;
        this.mrRepository = mrRepository;
        this.m50592Repository = m50592Repository;
        this.resultRepository =resultRepository;

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

        List<Train> trains = trainRepository.findBySiteAndDateFichierAndHeureFichier(site, dateFichier, heureTime);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);


        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            for (Result results : train.getResults()) {
            Map<String, Object> trainMap = new HashMap<>();

            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());




            trainMap.put("image", results.getImage());

            trainMap.put("imagemini", results.getThumbnail());



            boolean found50592 = false;


            for (M_50592 m50592 : m50592s) {
                if (heureTime .getHours() == m50592.getHeureFichier().getHours() &&
                        heureTime.getMinutes() == m50592.getHeureFichier().getMinutes() &&
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



            if (!found50592) {

                trainMap.put("url50592", null);

            }

            result.add(trainMap);
        }
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
            for (Result results : train.getResults()) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("numTrain", results.getEngine());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
//                trainMap.put("url", results.getImage();
                trainMap.put("site", site);

                boolean foundSam = false;
                boolean found50592 = false;

                for (Sam sam : sams) {
                    if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap.put("heuresam", sam.getHeureFichier());
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
                    if (train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours() &&
                            train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes() &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("heure50592", m50592.getHeureFichier());


                        trainMap.put("ber1", m50592.getBeR1());
                        trainMap.put("ber2", m50592.getBeR2());
                        trainMap.put("blr1", m50592.getBlR1());
                        trainMap.put("blr2", m50592.getBlR2());


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
                        System.out.println("size de mes bl " + parametreBLNode.size());
                        for (int i = 0; i < parametreBLNode.size(); i++) {
                            JsonNode entete = parametreBLNode.get(i).get(0);
                            JsonNode frequence = parametreBLNode.get(i).get(1);
                            entetesbl.put("entete" + i, entete);
                            frequencesbl.put("frequence" + i, frequence);
                        }

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);
                            JsonNode frequence = parametreBENode.get(i).get(1);
                            entetesbe.add(entete);
                            frequencesbe.add(frequence);
                        }


                        trainMap.put("entetesbl", entetesbl);
                        trainMap.put("frequencebl", entetesbl);

                        trainMap.put("entetes", entetesbe);
                        trainMap.put("frequence", frequencesbe);
                        ;

                        trainMap.put("outofband", outofband);
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
                    trainMap.put("statut50592", null);
                    trainMap.put("url50592", null);
                    trainMap.put("BE_R1", null);
                    trainMap.put("BE_R2", null);
                    trainMap.put("BL_R1", null);
                    trainMap.put("BL_R2", null);
                }

                Mr mr = mrRepository.findByNumTrain(results.getEngine());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }
                result.add(trainMap);
            }
        }







        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }




    // Méthode utilitaire pour vérifier si deux objets LocalTime sont identiques
    private boolean isSameTime(Date time1, Date time2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(time1);
        int hour1 = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute1 = calendar1.get(Calendar.MINUTE);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(time2);
        int hour2 = calendar2.get(Calendar.HOUR_OF_DAY);
        int minute2 = calendar2.get(Calendar.MINUTE);

        return hour1 == hour2 && minute1 == minute2;
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
        Set<Date> processedDates = new HashSet<>(); // Stocke les dates déjà traitées
        Set<Date> processedHours = new HashSet<>(); // Stocke les heures déjà traitées

        for (Train train : trains) {

            Map<String, Object> trainMap = new HashMap<>();

            Date dateKey = train.getDateFichier();
            Date hourKey = train.getHeureFichier();
            if (processedDates.contains(dateKey) && processedHours.contains(hourKey)) {
                continue; // Ignorer si la même date et heure ont déjà été traitées
            }

            for (Result results : train.getResults()) {
                trainMap.put("numTrain", results.getEngine());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("imagemini", results.getThumbnail());
                trainMap.put("site", site);
                Mr mr = mrRepository.findByNumTrain(results.getEngine());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                boolean foundSam = false;
                for (Sam sam : sams) {
                    if (isSameTime(train.getHeureFichier(), sam.getHeureFichier()) &&
                            train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("heuresam", sam.getHeureFichier());
                        trainMap.put("datesam", sam.getDateFichier());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("urlSam", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());

                        foundSam = true;
                        break;
                    }
                }

                boolean found50592 = false;
                for (M_50592 m50592 : m50592s) {
                    if (isSameTime(train.getHeureFichier(), m50592.getHeureFichier()) &&
                            train.getDateFichier().equals(m50592.getDateFichier())) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("heure50592", m50592.getHeureFichier());
                        trainMap.put("date50592", m50592.getDateFichier());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url50592", m50592.getUrl50592());
                        trainMap.put("ber1", m50592.getBeR1());
                        trainMap.put("ber2", m50592.getBeR2());
                        trainMap.put("blr1", m50592.getBlR1());
                        trainMap.put("blr2", m50592.getBlR2());

                        // Code commun pour les deux objets
                        Properties prop = new Properties();
                        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                        prop.load(input);

                        String outputFolderPath = prop.getProperty("output.folder.path");
                        File inputFile = new File(outputFolderPath, m50592.getFileName());

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                        JsonNode parametreBENode = rootNode.get("ParametresBE");
                        JsonNode parametreBLNode = rootNode.get("ParametresBL");
                        JsonNode outofband = rootNode.get("OutOfBand");
                        JsonNode pametreoutofband = rootNode.get("ParametresOutOfBand");
                        JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");

                        List<Object> enteteshb = new ArrayList<>();
                        List<Object> entetesbl = new ArrayList<>();
                        List<Object> frequencesbl = new ArrayList<>();
                        List<Object> entetesbe = new ArrayList<>();
                        List<Object> frequencesbe = new ArrayList<>();

                        for (int i = 0; i < parametreBLNode.size(); i++) {
                            JsonNode entete = parametreBLNode.get(i).get(0);
                            JsonNode frequence = parametreBLNode.get(i).get(1);
                            entetesbl.add(entete);
                            frequencesbl.add(frequence);
                        }

                        for (int i = 0; i < parametreBENode.size(); i++) {
                            JsonNode entete = parametreBENode.get(i).get(0);
                            JsonNode frequence = parametreBENode.get(i).get(1);
                            entetesbe.add(entete);
                            frequencesbe.add(frequence);
                        }

                        for (int i = 0; i < pametreoutofband.size(); i++) {
                            JsonNode entete = parametreBLNode.get(i).get(0);
                            enteteshb.add(entete);
                        }

                        trainMap.put("entetesbl", entetesbl);
                        trainMap.put("frequencebl", frequencesbl);
                        trainMap.put("entetesbe", entetesbe);
                        trainMap.put("frequencebe", frequencesbe);
                        trainMap.put("entetehorsbande", enteteshb);
                        trainMap.put("outofband", outofband);
                        trainMap.put("fondhorsbande", fondoutofband);
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
                    trainMap.put("heuresam", null);
                    trainMap.put("datesam", null);
                }

                if (!found50592) {
                    trainMap.put("meteo", null);
                    trainMap.put("statut50592", null);
                    trainMap.put("url50592", null);
                    trainMap.put("BE_R1", null);
                    trainMap.put("BE_R2", null);
                    trainMap.put("BL_R1", null);
                    trainMap.put("BL_R2", null);
                    trainMap.put("heure50592", null);
                    trainMap.put("date50592", null);
                }
            }
            processedDates.add(dateKey);
            processedHours.add(hourKey);
                result.add(trainMap);

        }



// Traiter les cas où sam n'est pas égal à train
        for (Sam sam : sams) {

            Date dateKey = sam.getDateFichier();
            Date hourKey = sam.getHeureFichier();
            if (processedDates.contains(dateKey) && processedHours.contains(hourKey)) {
                continue; // Ignorer si la même date et heure ont déjà été traitées
            }
            Map<String, Object> samTrainMap = new HashMap<>();
            samTrainMap.put("vitesse_moy", sam.getVitesse_moy());
            samTrainMap.put("heuresam", sam.getHeureFichier());
            samTrainMap.put("NbEssieux", sam.getNbEssieux());
            samTrainMap.put("urlSam", sam.getUrlSam());
            samTrainMap.put("statutSAM", sam.getStatutSAM());
            samTrainMap.put("NbOccultations", sam.getNbOccultations());
            samTrainMap.put("datesam", sam.getDateFichier());
            boolean foundTrain = false;
            boolean found50592 = false;
            for (Train train : trains) {
                if (isSameTime(train.getHeureFichier(), sam.getHeureFichier()) &&
                        train.getDateFichier().equals(sam.getDateFichier())) {
                    foundTrain = true;
                    break;
                }
            }


            for (M_50592 m50592 : m50592s) {
                if (isSameTime(m50592.getHeureFichier(), sam.getHeureFichier()) &&
                        m50592.getDateFichier().equals(sam.getDateFichier())) {
                    samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    samTrainMap.put("heure50592", m50592.getHeureFichier());
                    samTrainMap.put("date50592", m50592.getDateFichier());
                    samTrainMap.put("statut50592", m50592.getStatut50592());
                    samTrainMap.put("url50592", m50592.getUrl50592());
                    samTrainMap.put("ber1", m50592.getBeR1());
                    samTrainMap.put("ber2", m50592.getBeR2());
                    samTrainMap.put("blr1", m50592.getBlR1());
                    samTrainMap.put("blr2", m50592.getBlR2());

                    // Code commun pour les deux objets
                    Properties prop = new Properties();
                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                    prop.load(input);

                    String outputFolderPath = prop.getProperty("output.folder.path");
                    File inputFile = new File(outputFolderPath, m50592.getFileName());

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                    JsonNode parametreBENode = rootNode.get("ParametresBE");
                    JsonNode parametreBLNode = rootNode.get("ParametresBL");
                    JsonNode outofband = rootNode.get("OutOfBand");
                    JsonNode pametreoutofband = rootNode.get("ParametresOutOfBand");
                    JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");

                    List<Object> enteteshb = new ArrayList<>();
                    List<Object> entetesbl = new ArrayList<>();
                    List<Object> frequencesbl = new ArrayList<>();
                    List<Object> entetesbe = new ArrayList<>();
                    List<Object> frequencesbe = new ArrayList<>();

                    for (int i = 0; i < parametreBLNode.size(); i++) {
                        JsonNode entete = parametreBLNode.get(i).get(0);
                        JsonNode frequence = parametreBLNode.get(i).get(1);
                        entetesbl.add(entete);
                        frequencesbl.add(frequence);
                    }

                    for (int i = 0; i < parametreBENode.size(); i++) {
                        JsonNode entete = parametreBENode.get(i).get(0);
                        JsonNode frequence = parametreBENode.get(i).get(1);
                        entetesbe.add(entete);
                        frequencesbe.add(frequence);
                    }

                    for (int i = 0; i < pametreoutofband.size(); i++) {
                        JsonNode entete = parametreBLNode.get(i).get(0);
                        enteteshb.add(entete);
                    }

                    samTrainMap.put("entetesbl", entetesbl);
                    samTrainMap.put("frequencebl", frequencesbl);
                    samTrainMap.put("entetesbe", entetesbe);
                    samTrainMap.put("frequencebe", frequencesbe);
                    samTrainMap.put("entetehorsbande", enteteshb);
                    samTrainMap.put("outofband", outofband);
                    samTrainMap.put("fondhorsbande", fondoutofband);
                    found50592 = true;
                    break;
                }
            }

            if (foundTrain && found50592) {
                continue; // Ignorer si sam, train et 50592 sont égaux
            }

            if (!foundTrain) {

                samTrainMap.put("numTrain", null);
                samTrainMap.put("dateFichier", null);
                samTrainMap.put("heureFichier", null);
                samTrainMap.put("imagemini", null);
                samTrainMap.put("site", site);


            }


            if (!found50592) {

                samTrainMap.put("meteo", null);
                samTrainMap.put("statut50592", null);
                samTrainMap.put("url50592", null);
                samTrainMap.put("BE_R1", null);
                samTrainMap.put("BE_R2", null);
                samTrainMap.put("BL_R1", null);
                samTrainMap.put("BL_R2", null);
                samTrainMap.put("heure50592", null);
                samTrainMap.put("date50592", null);


            }
            processedDates.add(dateKey);
            processedHours.add(hourKey);
            result.add(samTrainMap);

        }




        // Traiter les cas où 50592 n'est pas égal à train


            for (M_50592 m50592 : m50592s) {
            Map<String, Object> samTrainMap = new HashMap<>();
                Date dateKey = m50592.getDateFichier();
                Date hourKey = m50592.getHeureFichier();
                if (processedDates.contains(dateKey) && processedHours.contains(hourKey)) {
                    continue; // Ignorer si la même date et heure ont déjà été traitées
                }
                samTrainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                samTrainMap.put("heure50592", m50592.getHeureFichier());
                samTrainMap.put("date50592", m50592.getDateFichier());
                samTrainMap.put("statut50592", m50592.getStatut50592());
                samTrainMap.put("url50592", m50592.getUrl50592());
                samTrainMap.put("ber1", m50592.getBeR1());
                samTrainMap.put("ber2", m50592.getBeR2());
                samTrainMap.put("blr1", m50592.getBlR1());
                samTrainMap.put("blr2", m50592.getBlR2());

                // Code commun pour les deux objets
                Properties prop = new Properties();
                InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
                prop.load(input);

                String outputFolderPath = prop.getProperty("output.folder.path");
                File inputFile = new File(outputFolderPath, m50592.getFileName());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class);
                JsonNode parametreBENode = rootNode.get("ParametresBE");
                JsonNode parametreBLNode = rootNode.get("ParametresBL");
                JsonNode outofband = rootNode.get("OutOfBand");
                JsonNode pametreoutofband = rootNode.get("ParametresOutOfBand");
                JsonNode fondoutofband = rootNode.get("OutOfBand_Fond");

                List<Object> enteteshb = new ArrayList<>();
                List<Object> entetesbl = new ArrayList<>();
                List<Object> frequencesbl = new ArrayList<>();
                List<Object> entetesbe = new ArrayList<>();
                List<Object> frequencesbe = new ArrayList<>();

                for (int i = 0; i < parametreBLNode.size(); i++) {
                    JsonNode entete = parametreBLNode.get(i).get(0);
                    JsonNode frequence = parametreBLNode.get(i).get(1);
                    entetesbl.add(entete);
                    frequencesbl.add(frequence);
                }

                for (int i = 0; i < parametreBENode.size(); i++) {
                    JsonNode entete = parametreBENode.get(i).get(0);
                    JsonNode frequence = parametreBENode.get(i).get(1);
                    entetesbe.add(entete);
                    frequencesbe.add(frequence);
                }

                for (int i = 0; i < pametreoutofband.size(); i++) {
                    JsonNode entete = parametreBLNode.get(i).get(0);
                    enteteshb.add(entete);
                }

                samTrainMap.put("entetesbl", entetesbl);
                samTrainMap.put("frequencebl", frequencesbl);
                samTrainMap.put("entetesbe", entetesbe);
                samTrainMap.put("frequencebe", frequencesbe);
                samTrainMap.put("entetehorsbande", enteteshb);
                samTrainMap.put("outofband", outofband);
                samTrainMap.put("fondhorsbande", fondoutofband);

            for (Train train : trains) {
                if (!isSameTime(train.getHeureFichier(), m50592.getHeureFichier()) &&
                        train.getDateFichier().equals(m50592.getDateFichier())) {
                    samTrainMap.put("numTrain", null);
                    samTrainMap.put("dateFichier", null);
                    samTrainMap.put("heureFichier", null);
                    samTrainMap.put("imagemini", null);
                    samTrainMap.put("site", site);

                }
            }


                for (Sam sam : sams) {
                    if (!isSameTime(sam.getHeureFichier(), m50592.getHeureFichier()) ||
                            !sam.getDateFichier().equals(m50592.getDateFichier())) {
                    samTrainMap.put("vitesse_moy", null);
                    samTrainMap.put("NbEssieux", null);
                    samTrainMap.put("urlSam", null);
                    samTrainMap.put("statutSAM", null);
                    samTrainMap.put("NbOccultations", null);
                    samTrainMap.put("tempsMs", null);
                    samTrainMap.put("heuresam", null);
                    samTrainMap.put("datesam", null);

                }
            }








                processedDates.add(dateKey);
                processedHours.add(hourKey);
            result.add(samTrainMap);

        }




        if (result.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(result);
}




//Api pour la partie rapoort automatique

//    @GetMapping("/dataBetweenRapport")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapport(
//            @RequestParam("site") String site,
//            @RequestParam(name = "statutsam", required = false) String statutSam,
//            @RequestParam(name = "statut50592", required = false) String statut50592 ,
//            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) throws IOException {
//
//        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
//        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetweenAndStatut50592(site, start, end, statut50592);
//        List<Sam> sams = samRepository.findBySiteAndDateFichierBetweenAndStatutSAM(site, start, end, statutSam);
//        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
//
//
//        int count = 0;
//        int count50 = 0;
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        Map<String, Object> trainMapSam = new HashMap<>();
//        Map<String, Object> trainMap50592 = new HashMap<>();
//        Map<String, Integer> m505952nokIndexValueMap = new HashMap<>();
//        Map<String, Integer> samnokIndexValueMap = new HashMap<>();
//        Map<String, Integer> redHeadersCountMap = new HashMap<>();
//        Map<Integer, Integer> redHeadersCountSamMap = new HashMap<>();
//        List<String> numTrains = new ArrayList<>();
//        List<String> Trainssamnok = new ArrayList<>();
//        List<String> Trainssamok = new ArrayList<>();
//        List<String> Trains50592ok = new ArrayList<>();
//        List<String> Trains50592nok = new ArrayList<>();
//
//
//        for (Train train : trains) {
//
//            List<Result> resultss = resultRepository.findResultsById(train.getId());
//            for (Result result1 : resultss) {
//            numTrains.add(result1.getEngine());
//
//            boolean allSamsOk = true;
//            for (Sam sam : sams) {
//                if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours()
//                        && train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes()
//                        && train.getDateFichier().equals(sam.getDateFichier())) {
//
//                    if (!sam.getStatutSAM().equals("OK")) {
//                        Trainssamnok.add(train.getResults().get(0).getEngine());
//
//                        if (sam.getNbOccultations() != null && sam.getNbOccultations().size() > 0) {
//                            for (int i = 0; i < sam.getNbOccultations().size(); i++) {
//                                if (!sam.getNbOccultations().get(i).equals(sam.getNbEssieux())) {
//                                    int index = i;
//                                    int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
//                                    redHeadersCountSamMap.put(index, occurrenceCount);
//                                }
//                            }
//                        }
//
//
//                    }
//
//
//                }
//            }
//            boolean all50592Ok = true;
//
//
//            for (M_50592 m50592 : m50592s) {
//                if (train.getHeureFichier().getHours() == m50592.getHeureFichier().getHours()
//                        && train.getHeureFichier().getMinutes() == m50592.getHeureFichier().getMinutes()
//                        && train.getDateFichier().equals(m50592.getDateFichier())) {
//
//                    Properties prop = new Properties();
//                    InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
//                    prop.load(input);
//
//                    String outputFolderPath = prop.getProperty("output.folder.path");
//
//                    File inputFile = new File(outputFolderPath, m50592.getFileName()); // use output folder path as parent directory
//                    ObjectMapper mapper = new ObjectMapper();
//                    JsonNode rootNode = mapper.readValue(inputFile, JsonNode.class); // read from input file
//                    JsonNode parametreBENode = rootNode.get("ParametresBE");
//                    if (!m50592.getStatut50592().equals("OK")) {
//                        Trains50592nok.add(result1.getEngine());
//
//                        for (int i = 0; i < parametreBENode.size(); i++) {
//                            JsonNode entete = parametreBENode.get(i).get(0);
//
//
//                            boolean isRedHeader = false;
//
//
//                            if (m50592.getBeR1().getxFond().get(i).equals("FF382A") || m50592.getBeR1().getyFond().get(i).equals("FF382A") || m50592.getBeR1().getzFond().get(i).equals("FF382A") || m50592.getBeR2().getxFond1().get(i).equals("FF382A") || m50592.getBeR2().getyFond1().get(i).equals("FF382A") || m50592.getBeR2().getzFond1().get(i).equals("FF382A")) {
//                                isRedHeader = true;
//                                String enteteValue = entete.asText();
//
//                                // Mise à jour du compteur pour l'en-tête rouge
//                                redHeadersCountMap.put(enteteValue, redHeadersCountMap.getOrDefault(enteteValue, 0) + 1);
//                            }
//                            if (!isRedHeader) {
//                                String enteteValue = entete.asText();
//                                redHeadersCountMap.putIfAbsent(enteteValue, 0);
//                            }
//
//
//                        }
//
//
//                    }
//
//                }
//
//            }
//
//
//            int countsamnok = Trainssamnok.size();
//
//            int count50592nok = Trains50592nok.size();
//
//
//            // Affichage des en-têtes rouges et leur nombre de fois 50592 not ok / 50592 ok
//            Map<String, Double> percentageMap = new HashMap<>();
//            for (Map.Entry<String, Integer> entry : redHeadersCountMap.entrySet()) {
//                String entete = entry.getKey();
//                Integer countbe = entry.getValue();
//
//                m505952nokIndexValueMap.put(entete, countbe);
//
//                if (!Trains50592nok.isEmpty()) {
//                    // Calcul du pourcentage
//                    double percentagenok = (double) countbe / count50592nok * 100;
//                    percentageMap.put(entete, percentagenok);
//                }
//
//            }
//
//
//            // Affichage des index et leur nombr de fois sam not ok
//            Map<String, Double> percentagesamnokMap = new HashMap<>();
//            for (Map.Entry<Integer, Integer> entry : redHeadersCountSamMap.entrySet()) {
//                int index = entry.getKey();
//                int countbe = entry.getValue();
//                System.out.println("Index : " + index + ", Nombre de fois : " + countbe);
//
//                // Ajout à samnokIndexValueMap
//                samnokIndexValueMap.put(String.valueOf(index), countbe);
//
//                // Calcul du pourcentage
//                double pourentagesam = (double) countbe / countsamnok * 100;
//                percentagesamnokMap.put(String.valueOf(index), pourentagesam);
//            }
//
//
//            //sam not ok
//            if (!Trainssamnok.isEmpty()) {
//
//                trainMapSam.put("nombre de train passé", count);
//
////                trainMapSam.put("nombre de train passé sam nok", countsamnok);
//                trainMapSam.put("index occultation et le total de fois de perturbation dans tous les trains  ", samnokIndexValueMap);
////                trainMap.put("somme de tous les types mrs", total);
//
//            }
//            //50592 not ok
//            if (!Trains50592nok.isEmpty()) {
//                trainMap50592.put("nombre de train passé", count);
//
//
//                trainMap50592.put("nombre de train passé 50592 nok", count50592nok);
//                trainMap50592.put("le poucentage de chaque capteur", percentageMap);
//                trainMap50592.put("nom du capteur et le nombre de perturbations", m505952nokIndexValueMap);
//
//            }
//
//
//            if (!trainMapSam.isEmpty()) {
//
//                result.add(trainMapSam);
//            }
//            if (!trainMap50592.isEmpty()) {
//                result.add(trainMap50592);
//            }
//
//
//        }
//
//    }
//        Map<String, Integer> indexcapteurCountMap = new HashMap<>();
//        int total50592nOk = 0;
//        Map<String, Integer> indexcapteurokCountMap = new HashMap<>();
//        int total50592Ok = 0;
//        Map<String, Object> totalPourcentageMap50592nok = new HashMap<>();
//        Map<String, Object> totalPourcentageMap50592ok = new HashMap<>();
//        Map<String, Object> totalPourcentageMapSamnok = new HashMap<>();
//        Map<String, Object> totalPourcentageMapSamok = new HashMap<>();
////50592 not ok
//        for (Map<String, Object> resultMap50592 : result) {
//            if (resultMap50592.containsKey("nom du capteur et le nombre de perturbations")) {
//
//                Map<String, Integer> pourcentage50592NOkMap = (Map<String, Integer>) resultMap50592.get("nom du capteur et le nombre de perturbations");
//
//                int train50592nOk = (int) resultMap50592.get("nombre de train passé 50592 nok");
//
//                total50592nOk += train50592nOk;
//                for (Map.Entry<String, Integer> entry : pourcentage50592NOkMap.entrySet()) {
//                    String capteur = entry.getKey();
//                    int totalFoisPerturbation = entry.getValue();
//
//                    int currentCount = indexcapteurCountMap.getOrDefault(capteur, 0);
//                    indexcapteurCountMap.put(capteur, currentCount + totalFoisPerturbation);
//                }
//            }
//
//
//
//
//            if (resultMap50592.containsKey("nom du capteur et le nombre de non perturbé")) {
//
//                Map<String, Integer> pourcentage50592OkMap = (Map<String, Integer>) resultMap50592.get("nom du capteur et le nombre de non perturbé");
//
//                int train50592Ok = (int) resultMap50592.get("nombre de train passé 50592 ok");
//
//                total50592Ok += train50592Ok;
//                for (Map.Entry<String, Integer> entry : pourcentage50592OkMap.entrySet()) {
//                    String capteur = entry.getKey();
//                    int totalFoisPerturbation = entry.getValue();
//
//                    int currentCount = indexcapteurokCountMap.getOrDefault(capteur, 0);
//                    indexcapteurokCountMap.put(capteur, currentCount + totalFoisPerturbation);
//                }
//            }
//
//        }
//
//
//
//
//// 50592 nok
//
//        Map<String, Object> totalPourcentage50592nok = new HashMap<>();
//        double percentage50592 = 0.0;
//// Calculate and display the percentages for each index of occultation
//        for (Map.Entry<String, Integer> entry : indexcapteurCountMap.entrySet()) {
//            String indexOccultation = entry.getKey();
//            int totalFoisPerturbation = entry.getValue();
//            percentage50592 = ((double) totalFoisPerturbation / total50592nOk) * 100;
//
//            totalPourcentage50592nok.put(indexOccultation, percentage50592);
//        }
//        if(!indexcapteurCountMap.isEmpty()){
//            totalPourcentageMap50592nok.put("total d'index capteurs", indexcapteurCountMap);
//            totalPourcentageMap50592nok.put("pourcentage des capteurs dans tous les types mr", totalPourcentage50592nok);
//
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//        //sam nok
//        Map<String, Integer> indexOccultationCountMap = new HashMap<>();
//        int totalSamnOk = 0;
//
//        for (Map<String, Object> resultMap : result) {
//            if (resultMap.containsKey("index occultation et le total de fois de perturbation dans tous les trains  ")) {
//                Map<String, Integer> indexOccultationMap = (Map<String, Integer>) resultMap.get("index occultation et le total de fois de perturbation dans tous les trains  ");
//
//                int trainSamnOk = (int) resultMap.get("nombre de train passé sam nok");
//
//                totalSamnOk += trainSamnOk;
//                System.out.println("je suis total ici  "+totalSamnOk);
//                for (Map.Entry<String, Integer> entry : indexOccultationMap.entrySet()) {
//                    String indexOccultation = entry.getKey();
//                    int totalFoisPerturbation = entry.getValue();
//
//                    int currentCount = indexOccultationCountMap.getOrDefault(indexOccultation, 0);
//                    indexOccultationCountMap.put(indexOccultation, currentCount + totalFoisPerturbation);
//                }
//            }
//        }
//        System.out.println("je suis total "+totalSamnOk);
//        Map<String, Object> totalPourcentageSamnok = new HashMap<>();
//        double percentage = 0.0;
//// Calculate and display the percentages for each index of occultation
//        for (Map.Entry<String, Integer> entry : indexOccultationCountMap.entrySet()) {
//            String indexOccultation = entry.getKey();
//            int totalFoisPerturbation = entry.getValue();
//            percentage = ((double) totalFoisPerturbation / totalSamnOk) * 100;
//
//            totalPourcentageSamnok.put(indexOccultation, percentage);
//        }
//        if(!indexOccultationCountMap.isEmpty()){
//            totalPourcentageMapSamnok.put("total d'index", indexOccultationCountMap);
//            totalPourcentageMapSamnok.put("pourcentage des EV dans tous les types mr", totalPourcentageSamnok);
//
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//        if(!totalPourcentageMapSamnok.isEmpty()){
//            result.add(totalPourcentageMapSamnok);
//        }
//
//        if(!totalPourcentageMap50592nok.isEmpty()){
//            result.add(totalPourcentageMap50592nok);
//        }
//
//
//
//
//
//
//        if (result.isEmpty()) {
//            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(result);
//
//
//
//    }




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
        List<M_50592> m50592s1 = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetweenAndStatutSAM(site, start, end,statutSam);
        List<Sam> samuniquement = samRepository.findBySiteAndDateFichierBetween(site, start, end);


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
                List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
                for (Train train : trains) {
                    Long trainId = train.getId(); // Récupérer l'id du train
                    List<Result> resultss = resultRepository.findByTrainIdAndEngine(trainId,mr.getNumTrain());

                    for (Result results : resultss) {
                        System.out.println("ress " + results.getDate());
                        numTrains.add(train.getResults().get(0).getEngine());

                        boolean allSamsOk = true;
                        for (Sam sam : sams) {
                            if (train.getHeureFichier().getHours() == sam.getHeureFichier().getHours()
                                    && train.getHeureFichier().getMinutes() == sam.getHeureFichier().getMinutes()
                                    && train.getDateFichier().equals(sam.getDateFichier())) {


                                if (!sam.getStatutSAM().equals("OK")) {
                                    Trainssamnok.add(train.getResults().get(0).getEngine());

                                    if (sam.getNbOccultations() != null && sam.getNbOccultations().size() > 0) {
                                        for (int i = 0; i < sam.getNbOccultations().size(); i++) {
                                            int index = i;
                                            if (!sam.getNbOccultations().get(i).equals(sam.getNbEssieux())) {

                                                int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                                redHeadersCountSamMap.put(index, occurrenceCount);
                                            } else {
                                                redHeadersCountSamMap.put(index, 0);
                                            }
                                        }
                                    }

                                    allSamsOk = false;
                                    break;
                                }


                            if (allSamsOk) {


                                System.out.println("sam " + sam.getStatutSAM());
                                Trainssamok.add(results.getEngine());


                            }
                        }
                    }
                    if (statutSam != null && !statutSam.isEmpty() && statutSam.equals("uniquement sam")) {
                        for (Sam sam1 : samuniquement) {
                            if (train.getHeureFichier().getHours() == sam1.getHeureFichier().getHours()
                                    && train.getHeureFichier().getMinutes() == sam1.getHeureFichier().getMinutes()
                                    && train.getDateFichier().equals(sam1.getDateFichier())) {

                                if (!sam1.getStatutSAM().equals("OK")) {
                                    Trainssamnok.add(train.getResults().get(0).getEngine());

                                    if (sam1.getNbOccultations() != null && sam1.getNbOccultations().size() > 0) {
                                        for (int i = 0; i < sam1.getNbOccultations().size(); i++) {
                                            int index = i;
                                            if (!sam1.getNbOccultations().get(i).equals(sam1.getNbEssieux())) {

                                                int occurrenceCount = redHeadersCountSamMap.getOrDefault(index, 0) + 1;
                                                redHeadersCountSamMap.put(index, occurrenceCount);
                                            } else {
                                                redHeadersCountSamMap.put(index, 0);
                                            }
                                        }
                                    }

                                    allSamsOk = false;
                                    break;
                                }


                                Trainssamok.add(results.getEngine());


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
                                Trains50592nok.add(results.getEngine());

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
                            }
                            if (all50592Ok) {
                                Trains50592ok.add(results.getEngine());

                            }

                        }

                    }

                    if (statut50592 != null && !statut50592.isEmpty() && statut50592.equals("uniquement 50592")) {
                        for (M_50592 m50592 : m50592s1) {

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
                                    Trains50592nok.add(results.getEngine());

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
                                }
                                Trains50592ok.add(results.getEngine());


                            }
                            break;
                        }

                    }


                }

                    }



            }
            Map<String, Object> trainMapSam = new HashMap<>();
            Map<String, Object> trainMap50592 = new HashMap<>();


            int countsamok = Trainssamok.size();
            int countsamnok = Trainssamnok.size();
            int count50592ok = Trains50592ok.size();
            int count50592nok = Trains50592nok.size();

            double pourcentagesamok = ((double) countsamok / (numTrains.size())) * 100;

            // Affichage des en-têtes rouges et leur nombre de fois 50592 not ok / 50592 ok
            Map<String, Double> percentageMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : redHeadersCountMap.entrySet()) {
                String entete = entry.getKey();
                Integer countbe = entry.getValue();

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

                trainMapSam.put("nombre de train passé (sam ok)", numTrains.size());
                trainMapSam.put("mr(sam ok)",typemr);
                trainMapSam.put("nombre de train passé avec sam ok", countsamok);
                trainMapSam.put("pourcentage de chaque type mr sam ok", pourcentagesamok);




            }


            // Affichage des index et leur nombr de fois sam not ok
            Map<String, Double> percentagesamnokMap = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : redHeadersCountSamMap.entrySet()) {
                int index = entry.getKey();
                int countbe = entry.getValue();


                // Ajout à samnokIndexValueMap
                samnokIndexValueMap.put(String.valueOf(index), countbe);

                // Calcul du pourcentage
                double pourentagesam = (double) countbe / countsamnok * 100;
                percentagesamnokMap.put(String.valueOf(index), pourentagesam);
            }





            //sam not ok
            if (!Trainssamnok.isEmpty()) {

                trainMapSam.put("nombre de train passé (sam nok)", numTrains.size());
                trainMapSam.put("mr(sam nok)",typemr);
                trainMapSam.put("nombre de train passé sam nok", countsamnok);
                trainMapSam.put("index occultation et le total de fois de perturbation dans tous les trains", samnokIndexValueMap);
                trainMapSam.put("pourcentage de perturbation par index d'un type mr", percentagesamnokMap);


            }
            //50592 not ok
            if (!Trains50592nok.isEmpty()) {
                trainMap50592.put("nombre de train passé(50592 nok)", numTrains.size());
                trainMap50592.put("mr(50592 nok)",typemr);
                trainMap50592.put("nombre de train passé 50592 nok", count50592nok);
                trainMap50592.put("le poucentage de chaque capteur",percentageMap);
                trainMap50592.put("nom du capteur et le nombre de perturbations", m505952nokIndexValueMap);

            }

            //50592  ok
            if (!Trains50592ok.isEmpty()) {
                trainMap50592.put("nombre de train passé(50592 ok )", numTrains.size());
                trainMap50592.put("mr (50592 ok)",typemr);
                trainMap50592.put("nombre de train passé 50592 ok", count50592ok);
                trainMap50592.put("le poucentage de chaque type mr (50592 ok)",percentageMap);

            }
if (!trainMapSam.isEmpty() ) {

    result.add(trainMapSam);
}
            if (!trainMap50592.isEmpty() ) {
                result.add(trainMap50592);
            }

        }
        double sommePourcentage50592Ok = 0.0;
        int trains50592Ok = 0;
        int trains50592 = 0;
        Map<String, Integer> indexcapteurCountMap = new HashMap<>();
        int total50592nOk = 0;
        Map<String, Integer> indexcapteurokCountMap = new HashMap<>();
        int total50592Ok = 0;
        Map<String, Object> totalPourcentageMap50592nok = new HashMap<>();
        Map<String, Object> totalPourcentageMap50592ok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamnok = new HashMap<>();
        Map<String, Object> totalPourcentageMapSamok = new HashMap<>();
//50592 not ok
        for (Map<String, Object> resultMap50592 : result) {
            if (resultMap50592.containsKey("nom du capteur et le nombre de perturbations")) {

                Map<String, Integer> pourcentage50592NOkMap = (Map<String, Integer>) resultMap50592.get("nom du capteur et le nombre de perturbations");

                int train50592nOk = (int) resultMap50592.get("nombre de train passé 50592 nok");

                total50592nOk += train50592nOk;
                for (Map.Entry<String, Integer> entry : pourcentage50592NOkMap.entrySet()) {
                    String capteur = entry.getKey();
                    int totalFoisPerturbation = entry.getValue();

                    int currentCount = indexcapteurCountMap.getOrDefault(capteur, 0);
                    indexcapteurCountMap.put(capteur, currentCount + totalFoisPerturbation);
                }
            }


           //50592 ok

            if (resultMap50592.containsKey("nombre de train passé 50592 ok") && resultMap50592.containsKey("nombre de train passé")) {
                int train50592Ok = (int) resultMap50592.get("nombre de train passé 50592 ok");
                int train50592 = (int) resultMap50592.get("nombre de train passé");

                trains50592Ok += train50592Ok;
                trains50592 += train50592;
            }

        }
        sommePourcentage50592Ok = ((double) trains50592Ok / trains50592) * 100;
        if (!Double.isNaN(sommePourcentage50592Ok)) {
            totalPourcentageMapSamok.put("le pourcentage de tous les 50592 ok et de tous les types mr", sommePourcentage50592Ok);
        }


// 50592 nok

        Map<String, Object> totalPourcentage50592nok = new HashMap<>();
        double percentage50592 = 0.0;
// Calculate and display the percentages for each index of occultation
        for (Map.Entry<String, Integer> entry : indexcapteurCountMap.entrySet()) {
            String indexOccultation = entry.getKey();
            int totalFoisPerturbation = entry.getValue();
            percentage50592 = ((double) totalFoisPerturbation / total50592nOk) * 100;

            totalPourcentage50592nok.put(indexOccultation, percentage50592);
        }
if(!indexcapteurCountMap.isEmpty()){
    totalPourcentageMap50592nok.put("total d'index capteurs", indexcapteurCountMap);
    totalPourcentageMap50592nok.put("pourcentage des capteurs dans tous les types mr", totalPourcentage50592nok);

}







            double sommePourcentageSamOk = 0.0;
            int trainsSamOk = 0;
            int trainsSam = 0;



//sam ok
            for (Map<String, Object> resultMap : result) {
                if (resultMap.containsKey("nombre de train passé avec sam ok") && resultMap.containsKey("nombre de train passé")) {
                    int trainSamOk = (int) resultMap.get("nombre de train passé avec sam ok");
                    int trainSam = (int) resultMap.get("nombre de train passé");

                    trainsSamOk += trainSamOk;
                    trainsSam += trainSam;
                }



            }

            sommePourcentageSamOk = ((double) trainsSamOk / trainsSam) * 100;
        if (!Double.isNaN(sommePourcentageSamOk)) {
            totalPourcentageMapSamok.put("le pourcentage de tous les sam ok et de tous les types mr", sommePourcentageSamOk);
        }


        //sam nok
            Map<String, Integer> indexOccultationCountMap = new HashMap<>();
            int totalSamnOk = 0;

            for (Map<String, Object> resultMap : result) {
                if (resultMap.containsKey("index occultation et le total de fois de perturbation dans tous les trains")) {
                    Map<String, Integer> indexOccultationMap = (Map<String, Integer>) resultMap.get("index occultation et le total de fois de perturbation dans tous les trains");

                    int trainSamnOk = (int) resultMap.get("nombre de train passé sam nok");

                    totalSamnOk += trainSamnOk;
                    System.out.println("je suis total ici  "+totalSamnOk);
                    for (Map.Entry<String, Integer> entry : indexOccultationMap.entrySet()) {
                        String indexOccultation = entry.getKey();
                        int totalFoisPerturbation = entry.getValue();

                        int currentCount = indexOccultationCountMap.getOrDefault(indexOccultation, 0);
                        indexOccultationCountMap.put(indexOccultation, currentCount + totalFoisPerturbation);
                    }
                }
            }
            System.out.println("je suis total "+totalSamnOk);
            Map<String, Object> totalPourcentageSamnok = new HashMap<>();
            double percentage = 0.0;
// Calculate and display the percentages for each index of occultation
            for (Map.Entry<String, Integer> entry : indexOccultationCountMap.entrySet()) {
                String indexOccultation = entry.getKey();
                int totalFoisPerturbation = entry.getValue();
                percentage = ((double) totalFoisPerturbation / totalSamnOk) * 100;

                totalPourcentageSamnok.put(indexOccultation, percentage);
            }
if(!indexOccultationCountMap.isEmpty()){
    totalPourcentageMapSamnok.put("total d'index", indexOccultationCountMap);
    totalPourcentageMapSamnok.put("pourcentage des EV dans tous les types mr", totalPourcentageSamnok);

}













if(!totalPourcentageMapSamnok.isEmpty()){
    result.add(totalPourcentageMapSamnok);
}

if(!totalPourcentageMap50592nok.isEmpty()){
    result.add(totalPourcentageMap50592nok);
}
        if(!totalPourcentageMap50592ok.isEmpty()){
            result.add(totalPourcentageMap50592ok);
        }
       if(!totalPourcentageMapSamok.isEmpty()){
           result.add(totalPourcentageMapSamok);
       }






        if (result.isEmpty()) {
            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.ok(result);
        }

    }

// api pour recuperer tous les types mr
    @GetMapping("/dataBetweenrMr")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetweenRapportmr(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Map<String, Integer>> categoryCountsByType = new HashMap<>();

        for (Train train : trains) {
            for (Result  results  : train.getResults()) {
            String trainNumber = results.getEngine();
            List<Mr> mrs = mrRepository.findAllByNumTrain(trainNumber);

            for (Mr mr : mrs) {
                String mrType = mr.getMr();
                String category = getCategory(mrType);

                // Count total occurrences of each category
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);

                // Count occurrences of each type within each category
                Map<String, Integer> typeCounts = categoryCountsByType.getOrDefault(category, new HashMap<>());
                typeCounts.put(mrType, typeCounts.getOrDefault(mrType, 0) + 1);
                categoryCountsByType.put(category, typeCounts);
            }
        }
    }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, Integer>> categoryEntry : categoryCountsByType.entrySet()) {
            String category = categoryEntry.getKey();
            Map<String, Integer> typeCounts = categoryEntry.getValue();

            for (Map.Entry<String, Integer> typeEntry : typeCounts.entrySet()) {
                String type = typeEntry.getKey();
                Integer count = typeEntry.getValue();

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("category", category);
                resultMap.put("typeMR", type);
                resultMap.put("count", count);
                result.add(resultMap);
            }
        }

        // Add "Other" category with count of MRs not falling into any specific category
        Integer otherCount = categoryCounts.getOrDefault("Other", 0);
        if (otherCount > 0) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("category", "Other");
            resultMap.put("typeMR", "Other");
            resultMap.put("count", otherCount);
            result.add(resultMap);
        }
        if (result.isEmpty()) {
            // Le résultat est vide, vous pouvez renvoyer une réponse spécifique
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    private String getCategory(String mrType) {
        if (mrType.startsWith("B")) {
            return "BB";
        } else if (mrType.startsWith("C")) {
            return "CC";
        } else if (mrType.startsWith("Z")) {
            return "Z";
        } else {
            return "Autre";
        }
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