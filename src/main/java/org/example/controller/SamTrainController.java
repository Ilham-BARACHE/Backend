package org.example.controller;

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
import java.sql.Time;
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


    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m_50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);


        if (sams.isEmpty() && trains.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (M_50592 m50592 : m_50592s) {
        for (Sam sam : sams) {

            for (Train train : trains) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("id", train.getId());
                trainMap.put("numTrain", train.getNumTrain());
                trainMap.put("dateFichier", train.getDateFichier());
                trainMap.put("heureFichier", train.getHeureFichier());
                trainMap.put("url",train.getUrl());

                trainMap.put("vitesse_moy", sam.getVitesse_moy());
                trainMap.put("id", sam.getId());
                trainMap.put("NbEssieux", sam.getNbEssieux());
                trainMap.put("urlSam", sam.getUrlSam());
                trainMap.put("statutSAM", sam.getStatutSAM());
                trainMap.put("NbOccultations", sam.getNbOccultations());



                trainMap.put("id", m50592.getId());
                trainMap.put("villeArrivee", m50592.getEnvironnement().getVilleArrivee());
                trainMap.put("villeDepart", m50592.getEnvironnement().getVilleDepart());
                trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                trainMap.put("statut50592",m50592.getStatut50592());
                trainMap.put("url50592",m50592.getUrl50592());



                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                result.add(trainMap);
            }
        }






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
                    trainMap.put("dateFichier", train.getDateFichier());
                    trainMap.put(("heureFichier"), train.getHeureFichier());

                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    trainMap.put("id", sam.getId());
                    trainMap.put("NbEssieux", sam.getNbEssieux());
                    trainMap.put("url", sam.getUrlSam());
                    trainMap.put("statutSAM", sam.getStatutSAM());
                    trainMap.put("NbOccultations", sam.getNbOccultations());



                    trainMap.put("id", m50592.getId());
                    trainMap.put("villeArrivee", m50592.getEnvironnement().getVilleArrivee());
                    trainMap.put("villeDepart", m50592.getEnvironnement().getVilleDepart());
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("statut50592",m50592.getStatut50592());

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


    @GetMapping("/dataBetweenDate")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start,end);
        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start , end);
        List<M_50592> m_50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start,end);


        if (sams.isEmpty() && trains.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (M_50592 m50592 : m_50592s) {
            for (Sam sam : sams) {

                for (Train train : trains) {
                    Map<String, Object> trainMap = new HashMap<>();
                    trainMap.put("id", train.getId());
                    trainMap.put("numTrain", train.getNumTrain());
                    trainMap.put("dateFichier", train.getDateFichier());
                    trainMap.put("heureFichier", train.getHeureFichier());
                    trainMap.put("url",train.getStatut());

                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    trainMap.put("id", sam.getId());
                    trainMap.put("NbEssieux", sam.getNbEssieux());
                    trainMap.put("url", sam.getUrlSam());
                    trainMap.put("statutSAM", sam.getStatutSAM());
                    trainMap.put("NbOccultations", sam.getNbOccultations());



                    trainMap.put("id", m50592.getId());
                    trainMap.put("villeArrivee", m50592.getEnvironnement().getVilleArrivee());
                    trainMap.put("villeDepart", m50592.getEnvironnement().getVilleDepart());
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("statut50592",m50592.getStatut50592());
                    trainMap.put("url",m50592.getUrl50592());



                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                    if (mr != null) {
                        trainMap.put("mr", mr.getMr());
                    }

                    result.add(trainMap);
                }
            }






        }


        return ResponseEntity.ok(result);
    }



}