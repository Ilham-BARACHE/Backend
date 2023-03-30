package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.example.component.SamAssembler;
import org.example.component.TrainAssembler;
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
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.TemporalType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private File getFileBySiteAndDateFichier(String site, Date dateFichier) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        ObjectMapper mapper = new ObjectMapper();
        String dateFichierStr = new SimpleDateFormat("yyyy.MM.dd").format(dateFichier);

        File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005-" + site + "_" + dateFichierStr) && name.endsWith(".json"));

        if (samFiles != null && samFiles.length > 0) {
            return samFiles[0];
        } else {
            return null;
        }
    }

    @GetMapping("/temps")
    public List<JsonNode> getTempsMs(@RequestParam("site") String site, @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        File file = getFileBySiteAndDateFichier(site, dateFichier);
        List<JsonNode> tempsMsNodesList = new ArrayList<>();

        if (file != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(file);
            JsonNode tempsMsNodes = rootNode.get("Temps_ms");

            for (JsonNode tempsMsNode : tempsMsNodes) {
                tempsMsNodesList.add(tempsMsNode);
                System.out.println(tempsMsNode);
            }
        }

        System.out.println(tempsMsNodesList);
        return tempsMsNodesList;
    }


    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);
        List<JsonNode> tempsMsNodesList = getTempsMs(site,date);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();
            trainMap.put("numTrain", train.getNumTrain());
            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());
            trainMap.put("url", train.getUrl());

            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().equals(sam.getHeureFichier())) {
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    trainMap.put("NbEssieux", sam.getNbEssieux());
                    trainMap.put("urlSam", sam.getUrlSam());
                    trainMap.put("statutSAM", sam.getStatutSAM());
                    trainMap.put("NbOccultations", sam.getNbOccultations());
//                    trainMap.put("tempsMs", tempsMsNodesList);
                    foundSam = true;
                    break;
                }
            }

            for (M_50592 m50592 : m50592s) {
                if (train.getHeureFichier().equals(m50592.getHeureFichier())) {
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("statut50592", m50592.getStatut50592());
                    trainMap.put("url50592", m50592.getUrl50592());
//                    trainMap.put("BE_R1",m50592.getBE_R1());
//                    trainMap.put("BE_R2",m50592.getBeR2());
//                    trainMap.put("BL_R1",m50592.getBlR1());
//                    trainMap.put("BL_R2",m50592.getBlR2());
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




    @GetMapping("/info")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierheure(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);

        // Créer un Set pour stocker toutes les heures des enregistrements
        Set<Date> heuresCommunes = new HashSet<>();
        for (Sam sam : sams) {
            heuresCommunes.add(sam.getHeureFichier());
        }
        for (Train train : trains) {
            heuresCommunes.add(train.getHeureFichier());
        }
        for (M_50592 m50592 : m50592s) {
            heuresCommunes.add(m50592.getHeureFichier());
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // Itérer sur les heures communes et récupérer les enregistrements correspondants
        for (Date heureCommune : heuresCommunes) {
            List<Sam> samsHeureCommune = sams.stream()
                    .filter(sam -> sam.getHeureFichier().equals(heureCommune))
                    .collect(Collectors.toList());
            List<Train> trainsHeureCommune = trains.stream()
                    .filter(train -> train.getHeureFichier().equals(heureCommune))
                    .collect(Collectors.toList());
            List<M_50592> m50592sHeureCommune = m50592s.stream()
                    .filter(m50592 -> m50592.getHeureFichier().equals(heureCommune))
                    .collect(Collectors.toList());

            // Ajouter les enregistrements correspondants dans la liste result
            for (Sam sam : samsHeureCommune) {


                ;for (M_50592 m50592 : m50592sHeureCommune) {


                for (Train train : trainsHeureCommune) {
                    Map<String, Object> trainMap = new HashMap<>();
                    trainMap.put("id", train.getId());
                    trainMap.put("numTrain", train.getNumTrain());


                    if (sam != null) {
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    } else {
                        trainMap.put("vitesse_moy", null);
                    }

                    if (m50592 != null) {
                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    } else {
                        trainMap.put("meteo", null);
                    }

                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                    if (mr != null) {
                        trainMap.put("mr", mr.getMr());
                    }

                    result.add(trainMap);
                }
            }





            }

        }

        return ResponseEntity.ok(result);


    }


    @GetMapping("/dataBetweenDate50592")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<M_50592> m_50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
        if (m_50592s.isEmpty()) {
            // Si la liste de fichiers 50592 est vide, renvoyer une réponse avec un corps vide ou avec un message "null"
            return ResponseEntity.ok(null); // ou return ResponseEntity.noContent().build();
        }
        if (m_50592s.isEmpty() && trains.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (M_50592 m50592 : m_50592s) {
            if (m50592.getStatut50592().equals("NOK")) {
                for (Train train : trains) {
                    if (train.getDateFichier().equals(m50592.getDateFichier()) &&
                            train.getHeureFichier().equals(m50592.getHeureFichier())) {
                        Map<String, Object> trainMap = new HashMap<>();
                        trainMap.put("numTrain", train.getNumTrain());
                        trainMap.put("dateFichier", train.getDateFichier());
                        trainMap.put("heureFichier", train.getHeureFichier());
                        trainMap.put("url", train.getUrl());



                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                        trainMap.put("statut50592", m50592.getStatut50592());
                        trainMap.put("url_50592", m50592.getUrl50592());

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