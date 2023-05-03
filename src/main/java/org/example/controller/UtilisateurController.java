package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jdk.jshell.execution.Util;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;


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

    @PostMapping("/someEndpoint")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> someEndpoint(@RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = authorizationHeader.replace("Bearer ", "");

        try {
            Jwts.parser().setSigningKey("secret_key").parseClaimsJws(jwtToken);
        } catch (JwtException e) {
            // Token est invalide ou expiré
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Token est valide
        Claims claims = Jwts.parser().setSigningKey("secret_key").parseClaimsJws(jwtToken).getBody();
        String role = claims.get("role", String.class);

        if (!role.equals("ADMIN")) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Faites quelque chose avec le rôle et le prénom de l'utilisateur
        // ...

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }


    @PostMapping("/connexion")
    public ResponseEntity<String> login(@Valid @RequestBody Utilisateur user) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String login = user.getLogin();
        String password = user.getPassword();   // Vérifiez si l'utilisateur avec l'email spécifié existe dans la base de données
        String hashedPassword = DigestUtils.sha256Hex(password);

        Utilisateur utilisateur = utilisateurRepository.findByLogin(login);
        System.out.println(utilisateur);
        if (utilisateur == null) {
            return new ResponseEntity<>("L'utilisateur n'existe pas", HttpStatus.UNAUTHORIZED);
        }

        // Vérifiez si le mot de passe est correct pour l'utilisateur spécifié
        if (!utilisateur.getPassword().equals(hashedPassword)) {
            return new ResponseEntity<>("Mot de passe incorrect", HttpStatus.UNAUTHORIZED);
        }

        String secretKey = "sncfihm2023adent";
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        String role = utilisateur.getRole();
        byte[] encryptedRoleBytes = cipher.doFinal(role.getBytes());
        String encryptedRole = Base64.getEncoder().encodeToString(encryptedRoleBytes);





        String etat = utilisateur.getEtat();
        byte[] encryptedEtatBytes = cipher.doFinal(etat.getBytes());
        String encryptedEtat = Base64.getEncoder().encodeToString(encryptedEtatBytes);



        String prenom = utilisateur.getPrenom();
        byte[] encryptedPrenomBytes = cipher.doFinal(prenom.getBytes());
        String encryptedPrenom = Base64.getEncoder().encodeToString(encryptedPrenomBytes);


        String token = Jwts.builder()
                .setSubject(utilisateur.getLogin())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, "secret_key")
                .compact();

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("a", token);
        jsonResponse.addProperty("b", encryptedPrenom);
        jsonResponse.addProperty("c", encryptedRole);
        jsonResponse.addProperty("d", encryptedEtat);


// Ajouter l'objet JSON à l'en-tête de la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("a", "Bearer " + token);
        headers.add("c", encryptedRole);
        headers.add("b", encryptedPrenom);
        headers.add("d", encryptedEtat );
        headers.add("Access-Control-Expose-Headers", "a, c");
        headers.add("X-Content-Type-Options", "nosniff");

//        // récupérer le texte chiffré depuis la réponse JSON
//        String encryptedRole1 = jsonResponse.get("c").getAsString();
//
//// décoder le texte chiffré avec Base64
//        byte[] encryptedRoleBytes1 = Base64.getDecoder().decode(encryptedRole1);
//
//// déchiffrer le texte avec AES
//        SecretKeySpec secretKeySpec1 = new SecretKeySpec(secretKey.getBytes(), "AES");
//        Cipher cipher1 = Cipher.getInstance("AES");
//        cipher1.init(Cipher.DECRYPT_MODE, secretKeySpec1);
//        byte[] decryptedRoleBytes = cipher1.doFinal(encryptedRoleBytes1);
//
//// convertir le texte déchiffré en chaîne de caractères
//        String decryptedRole = new String(decryptedRoleBytes);
//
//// afficher la chaîne de caractères déchiffrée
//        System.out.println(decryptedRole);

// Renvoyer une réponse réussie avec l'en-tête d'autorisation et le corps JSON
        return new ResponseEntity<>(jsonResponse.toString(), headers, HttpStatus.OK);

    }










    @GetMapping("/user")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getAllUser() {
        try {
            List<EntityModel<Utilisateur>> users = utilisateurRepository.findAll().stream()
                    .map(utilisateur -> {
                        Utilisateur utilisateurSansPassword = new Utilisateur();
                        utilisateurSansPassword.setId(utilisateur.getId());
                        utilisateurSansPassword.setNom(utilisateur.getNom());
                        utilisateurSansPassword.setPrenom(utilisateur.getPrenom());
                        utilisateurSansPassword.setLogin(utilisateur.getLogin());
                        utilisateurSansPassword.setSite(utilisateur.getSite());
                        utilisateurSansPassword.setRole(utilisateur.getRole());
                        utilisateurSansPassword.setEtat((utilisateur.getEtat()));

                        return utilisateurAssembler.toModel(utilisateurSansPassword);
                    })
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
        Utilisateur userSansPassword = new Utilisateur();
        userSansPassword.setId(user.getId());
        userSansPassword.setNom(user.getNom());
        userSansPassword.setPrenom(user.getPrenom());
        userSansPassword.setLogin(user.getLogin());
        userSansPassword.setSite(user.getSite());
        userSansPassword.setRole(user.getRole());
        userSansPassword.setEtat(user.getEtat());
        return new ResponseEntity<>(utilisateurAssembler.toModel(userSansPassword), HttpStatus.OK);
    }



    @PostMapping("/NewUser")
    public ResponseEntity<?> createUser(@Valid @RequestBody Utilisateur user){
        try{
            if (utilisateurRepository.exists(user.getLogin())){
                return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
            } else {
                String hashedPassword = DigestUtils.sha256Hex(user.getPassword());
                user.setPassword(hashedPassword);
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
        String password = user.getPassword();   // Vérifiez si l'utilisateur avec l'email spécifié existe dans la base de données
        String hashedPassword = DigestUtils.sha256Hex(password);
        if (utilisateurRepository.exists(user, id)){
            return new ResponseEntity<>("Utilisateur existe", HttpStatus.CONFLICT);
        }
        Utilisateur utilisateurData = utilisateurRepository.findById(id)
                .map(utilisateur -> {
                    utilisateur.setEtat(user.getEtat());

                    utilisateur.setPassword(hashedPassword);

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
