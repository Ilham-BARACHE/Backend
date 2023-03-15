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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.TemporalType;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/")
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

        for (Sam sam : sams) {
            Map<String, Object> samMap = new HashMap<>();
            samMap.put("id", sam.getId());
            samMap.put("NbEssieux", sam.getNbEssieux());
            samMap.put("url", sam.getUrl());
            samMap.put("Statut", sam.getStatut());
            samMap.put("NbOccultations", sam.getNbOccultations());
            samMap.put("vitesse_moy", sam.getVitesse_moy());
            result.add(samMap);
        }

        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();
            trainMap.put("id", train.getId());
            trainMap.put("numTrain", train.getNumTrain());

            Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
            if (mr != null) {
                trainMap.put("mr", mr.getMr());
            }

            result.add(trainMap);
        }

        for (M_50592 m50592 : m_50592s) {
            Map<String, Object> m50592Map = new HashMap<>();
            m50592Map.put("id", m50592.getId());
            m50592Map.put("villeArrivee", m50592.getEnvironnement().getVilleArrivee());
            m50592Map.put("villeDepart", m50592.getEnvironnement().getVilleDepart());
            m50592Map.put("meteo", m50592.getEnvironnement().getMeteo());


            result.add(m50592Map);
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
                Map<String, Object> samMap = new HashMap<>();
                samMap.put("id", sam.getId());
                samMap.put("NbEssieux", sam.getNbEssieux());
                samMap.put("url", sam.getUrl());
                samMap.put("Statut", sam.getStatut());
                samMap.put("NbOccultations", sam.getNbOccultations());
                samMap.put("vitesse_moy", sam.getVitesse_moy());
                result.add(samMap);
            }

            for (Train train : trainsHeureCommune) {
                Map<String, Object> trainMap = new HashMap<>();
                trainMap.put("id", train.getId());
                trainMap.put("numTrain", train.getNumTrain());

                Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                if (mr != null) {
                    trainMap.put("mr", mr.getMr());
                }

                result.add(trainMap);
            }

            for (M_50592 m50592 : m50592sHeureCommune) {
                Map<String, Object> m50592Map = new HashMap<>();
                m50592Map.put("id", m50592.getId());
                m50592Map.put("villeArrivee", m50592.getEnvironnement().getVilleArrivee());
                m50592Map.put("villeDepart", m50592.getEnvironnement().getVilleDepart());
                m50592Map.put("meteo", m50592.getEnvironnement().getMeteo());


                result.add(m50592Map);
            }

        }

        return ResponseEntity.ok(result);


    }

}