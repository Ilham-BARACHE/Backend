package org.example.controller;


import jdk.jshell.execution.Util;
import org.example.component.SamAssembler;
import org.example.component.UtilisateurAssembler;
import org.example.component.Utils;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Sam;
import org.example.model.Utilisateur;
import org.example.repository.SamRepository;
import org.example.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class UtilisateurController {

    @Autowired
    private Utils utils;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurAssembler utilisateurAssembler;


    public UtilisateurController(UtilisateurRepository utilisateurRepository, UtilisateurAssembler utilisateurAssembler) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurAssembler = utilisateurAssembler;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllUser() {
        try {
            List<EntityModel<Utilisateur>> users = utilisateurRepository.findAll().stream()
                    .map(utilisateurAssembler::toModel)
                    .collect(Collectors.toList());
            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(CollectionModel.of(users,
                    linkTo(methodOn(SamController.class).getAllConf()).withSelfRel()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<EntityModel<Utilisateur>> getUserById(@PathVariable(value = "id") Long id){
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossible de trouver l'utilisateur' " + id));
        return new ResponseEntity<>(utilisateurAssembler.toModel(user), HttpStatus.OK);
    }
    @PostMapping("/NewUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody Utilisateur user){
        try{
            if (utilisateurRepository.exists(user.getLogin())){
                return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
            } else {
                EntityModel<Utilisateur> entityModel = utilisateurAssembler.toModel(utilisateurRepository.save(user));
                return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
            }
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @PutMapping("/updateuser/{id}")
    public ResponseEntity<?> updateUser(@Valid @RequestBody Utilisateur user,
                                        @PathVariable(value = "id") Long id){
        if (utilisateurRepository.exists(user, id)){
            return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
        }
        Utilisateur utilisateurData = utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    utilisateur.setEtat(user.getEtat());
                    utilisateur.setLogin(user.getLogin());
                    utilisateur.setPassword(user.getPassword());
                    utilisateur.setNom(user.getNom());
                    utilisateur.setPrenom(user.getPrenom());
                    utilisateur.setRole(user.getRole());
                    return utilisateurRepository.save(utilisateur);
                })
                .orElseGet(() -> {
                    user.setId(id);
                    return utilisateurRepository.save(user);
                });
        EntityModel<Utilisateur> entityModel = utilisateurAssembler.toModel(utilisateurData);
        return new ResponseEntity<>(entityModel, HttpStatus.OK);
    }


    @DeleteMapping("/deleteuser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "id") Long id){
        try{
            utilisateurRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
