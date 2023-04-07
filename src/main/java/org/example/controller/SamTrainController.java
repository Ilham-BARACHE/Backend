package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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
                if (train.getHeureFichier().equals(sam.getHeureFichier())) {

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
                if (train.getHeureFichier().equals(m50592.getHeureFichier())) {

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
                                    imagestrain.add(image);
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
                if (train.getHeureFichier().equals(sam.getHeureFichier())) {
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
                if (train.getHeureFichier().equals(m50592.getHeureFichier())) {
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("statut50592", m50592.getStatut50592());
                    trainMap.put("url50592", m50592.getUrl50592());


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




//    @GetMapping("/info")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierheure(
//            @RequestParam("site") String site,
//            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
//
//        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
//        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
//        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);
//
//        // Créer un Set pour stocker toutes les heures des enregistrements
//        Set<Date> heuresCommunes = new HashSet<>();
//        for (Sam sam : sams) {
//            heuresCommunes.add(sam.getHeureFichier());
//        }
//        for (Train train : trains) {
//            heuresCommunes.add(train.getHeureFichier());
//        }
//        for (M_50592 m50592 : m50592s) {
//            heuresCommunes.add(m50592.getHeureFichier());
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        // Itérer sur les heures communes et récupérer les enregistrements correspondants
//        for (Date heureCommune : heuresCommunes) {
//            List<Sam> samsHeureCommune = sams.stream()
//                    .filter(sam -> sam.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//            List<Train> trainsHeureCommune = trains.stream()
//                    .filter(train -> train.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//            List<M_50592> m50592sHeureCommune = m50592s.stream()
//                    .filter(m50592 -> m50592.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//
//            // Ajouter les enregistrements correspondants dans la liste result
//            for (Sam sam : samsHeureCommune) {
//
//
//                ;for (M_50592 m50592 : m50592sHeureCommune) {
//
//
//                for (Train train : trainsHeureCommune) {
//                    Map<String, Object> trainMap = new HashMap<>();
//                    trainMap.put("id", train.getId());
//                    trainMap.put("numTrain", train.getNumTrain());
//
//
//                    if (sam != null) {
//                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                    } else {
//                        trainMap.put("vitesse_moy", null);
//                    }
//
//                    if (m50592 != null) {
//                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                    } else {
//                        trainMap.put("meteo", null);
//                    }
//
//                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
//                    if (mr != null) {
//                        trainMap.put("mr", mr.getMr());
//                    }
//
//                    result.add(trainMap);
//                }
//            }
//
//
//
//
//
//            }
//
//        }
//
//        return ResponseEntity.ok(result);
//
//
//    }









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
            if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
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
            if (train.getHeureFichier().equals(m50592.getHeureFichier()) && train.getDateFichier().equals(m50592.getDateFichier())) {
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


    @GetMapping("/dataBetweenDateSAM")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAMBetween(
            @RequestParam("site") String site,
            @RequestParam("statut") String statutSam,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Sam sam : sams) {
            if (!statutSam.equals("uniquement sam") && !sam.getStatutSAM().equals(statutSam)) {
                continue; // Si le statut de ce SAM n'est pas le statut demandé, on passe au prochain SAM
            }
            Map<String, Object> trainMap = null;
            for (Train train : trains) {
                if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
                    trainMap = new HashMap<>();
                    trainMap.put("numTrain", train.getNumTrain());
                    trainMap.put("dateFichier", train.getDateFichier());
                    trainMap.put("heureFichier", train.getHeureFichier());
                    trainMap.put("url", train.getUrl());
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());


                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                    if (mr != null) {
                        trainMap.put("mr", mr.getMr());
                    }
                    result.add(trainMap);
                    break; // On a trouvé le train correspondant à ce SAM, on passe au prochain SAM
                }
            }
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }





    @GetMapping("/dataBetweenDatesam")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAM(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

        if (sams.isEmpty() && trains.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Sam sam : sams) {
            if (sam.getStatutSAM().equals("NOK")) {
                for (Train train : trains) {
                    if (train.getDateFichier().equals(sam.getDateFichier()) &&
                            train.getHeureFichier().equals(sam.getHeureFichier())) {
                        Map<String, Object> trainMap = new HashMap<>();
                        trainMap.put("id", train.getId());
                        trainMap.put("numTrain", train.getNumTrain());
                        trainMap.put("dateFichier", train.getDateFichier());
                        trainMap.put("heureFichier", train.getHeureFichier());
                        trainMap.put("url", train.getStatut());



                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("id", sam.getId());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("url", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());

                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                        if (mr != null) {
                            trainMap.put("mr", mr.getMr());
                        }

                        result.add(trainMap);
                        break;
                    }
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    private int lastMonthOfQuarter(int month) {
        if (month <= 0 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        int quarter = (month - 1) / 3 + 1; // calcul du numéro de trimestre
        return quarter * 3; // le dernier mois du trimestre est le mois numéro 3, 6, 9 ou 12
    }

    @GetMapping("/dataQuarterlySAM")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndQuarterSAM(

    ) throws Exception {

        LocalDate startDate = LocalDate.of(2023, 1, 1);

        List<Map<String, Object>> result = new ArrayList<>();

        while (true) {
            LocalDate endDate = startDate.plusMonths(3);
            if (endDate.getYear() != startDate.getYear()) {
                endDate = endDate.withYear(startDate.getYear());
            }
            System.out.println("Trimestre " + ((endDate.getMonthValue() + 2) / 3) + " " + startDate + " - " + endDate.minusDays(1));


            int lastMonth = lastMonthOfQuarter(endDate.getMonthValue()); // on utilise endDate au lieu de startDate
            LocalDate endQuarterDate = LocalDate.of(endDate.getYear(), lastMonth, 30).with(TemporalAdjusters.lastDayOfMonth()); // on calcule la fin du trimestre
            System.out.println("Fin de trimestre: " + endQuarterDate);


            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Train> trains = trainRepository.findAll();

            List<Sam> sams = samRepository.findAll();
//            List<List<JsonNode>> tempsMsNodesList = getTempsMsBetween(site, startDate, endDate);

            for (Sam sam : sams) {

                Map<String, Object> trainMap = null;
                for (Train train : trains) {
                    if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap = new HashMap<>();
                        trainMap.put("numTrain", train.getNumTrain());
                        trainMap.put("dateFichier", train.getDateFichier());
                        trainMap.put("heureFichier", train.getHeureFichier());
//                        trainMap.put("url", train.getUrl());
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("datesam" ,sam.getDateFichier());
//                        List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
//                        trainMap.put("tempsMs", tempsList);

                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                        if (mr != null) {
                            trainMap.put("mr", mr.getMr());
                        }
                        result.add(trainMap);
                        break; // On a trouvé le train correspondant à ce SAM, on passe au prochain SAM
                    }
                }
            }

            if (endDate.isAfter(LocalDate.now())) {
                break;
            }
            startDate = endDate;
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }





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