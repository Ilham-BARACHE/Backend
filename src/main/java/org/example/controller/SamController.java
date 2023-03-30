package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.component.SamAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Sam;
import org.example.repository.SamRepository;
import org.example.service.SamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController



public class SamController {
    @Autowired
    private Utils utils;
    private final SamRepository samRepository;
    private final SamAssembler samAssembler;

    public SamController(SamRepository samRepository, SamAssembler samAssembler) {
        this.samRepository = samRepository;
        this.samAssembler = samAssembler;
    }

    @GetMapping("/SAM")
    public ResponseEntity<?> getAllConf() {
        try {
            List<EntityModel<Sam>> confs = samRepository.findAll().stream()
                    .map(samAssembler::toModel)
                    .collect(Collectors.toList());
            if (confs.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(confs,
                    linkTo(methodOn(SamController.class).getAllConf()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/SAM/{id}")
    public ResponseEntity<EntityModel<Sam>> getConfById(@PathVariable(value = "id") Long id) {
        Sam conf = samRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));
        return new ResponseEntity<>(samAssembler.toModel(conf), HttpStatus.OK);
    }


    // Endpoint pour envoyer les temps_ms à une API externe
//    @PostMapping("/envoyer_temps_ms")
//    public ResponseEntity<String> envoyerTempsMs(@RequestBody List<Double> tempsMsList) {
//        // Récupérer l'URL de l'API externe
//        String apiUrl = "https://exemple.com/api";
//
//        // Envoyer les temps_ms à l'API externe
//        try {
//            HttpResponse response = Request.Post(apiUrl)
//                    .bodyString(new Gson().toJson(tempsMsList), ContentType.APPLICATION_JSON)
//                    .execute().returnResponse();
//            return ResponseEntity.ok(response.toString());
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//}




//    @GetMapping("/SAM/Date")
//    public List<Sam> getFilmsBetweenDates(
//            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
//            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
//        return samRepository.findByDateBetween(startDate, endDate);
//
//    }
}