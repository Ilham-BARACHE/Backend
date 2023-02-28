package org.example.controller;

import org.example.component.SamAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Sam;
import org.example.repository.SamRepository;
import org.example.service.SamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/")


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
    public ResponseEntity<?> getAllConf(){
        try {
            List<EntityModel<Sam>> confs =  samRepository.findAll().stream()
                    .map(samAssembler::toModel)
                    .collect(Collectors.toList());
            if (confs.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(confs,
                    linkTo(methodOn(SamController.class).getAllConf()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/SAM/{id}")
    public ResponseEntity<EntityModel<Sam>> getConfById(@PathVariable(value = "id") Long id){
        Sam conf = samRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver la configuration des marques habilitées par activité " + id));
        return new ResponseEntity<>(samAssembler.toModel(conf), HttpStatus.OK);
    }

}
