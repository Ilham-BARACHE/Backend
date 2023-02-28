package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.M_50592;
import org.example.model.Sam;
import org.example.model.Train;
import org.example.service.M_50592Service;
import org.example.service.SamService;
import org.example.service.TrainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Time;
import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }



    @Bean
    CommandLineRunner runner(SamService samService, M_50592Service m50592Service , TrainService trainService) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            File inputFolder = new File("C:\\Users\\Ilham Barache\\Documents\\input");
            File outputFolder = new File("C:\\Users\\Ilham Barache\\Documents\\output");

            // Lire tous les fichiers commençant par 'Sam'
            File[] samFiles = inputFolder.listFiles((dir, name) -> name.startsWith("SAM"));
            for (File samFile : samFiles) {
                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {};
                try (InputStream samStream = new FileInputStream(samFile)) {
                    List<Sam> sams = mapper.readValue(samStream, samTypeRef);
                    samService.save(sams);
                    System.out.println("Le fichier " + samFile.getName() + " a été traité avec succès !");

                    for (Sam sam : sams) {
                        Train train = new Train();
                        train.setNb_Essieux(sam.getNbEssieux());
                        train.setId(sam.getId());
                        trainService.save(train);
                        sam.setTrain(train);
                        samService.save(sam);
                        train.setSam(sam);
                        sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        samService.save(sam);
                    }




                    // Déplacer le fichier traité dans le dossier 'output'
                    //Path sourceFilePath = samFile.toPath();
                  //  Path targetFilePath = outputFolder.toPath().resolve(samFile.getName());
                  //  Files.move(sourceFilePath, targetFilePath);
                   // System.out.println("Le fichier " + samFile.getName() + " a été déplacé dans le dossier 'output'.");
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage());
                }
            }



            // Lire tous les fichiers commençant par '50592'
            File[] m50592Files = inputFolder.listFiles((dir, name) -> name.startsWith("50592"));
            for (File myClassFile : m50592Files) {
                TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {};
                try (InputStream m50592Stream = new FileInputStream(myClassFile)) {
                    List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);
                    m50592Service.save(m_50592s);
                    System.out.println("Le fichier " + myClassFile.getName() + " a été traité avec succès !");


                    for (M_50592 m_50592 : m_50592s) {

                        m_50592.setFileName(myClassFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        String filename = myClassFile.getName();
                        String[] parts = filename.split("_");
                        if (parts.length > 1) {
                            String dateTimePart = parts[1];
                            String[] dateTimeParts = dateTimePart.split("[HhMmSs]");
                            if (dateTimeParts.length == 6) {
                                String datePart = dateTimeParts[0] + "." + dateTimeParts[1] + "." + dateTimeParts[2];
                                String heurePart = dateTimeParts[3] + ":" + dateTimeParts[4] + ":" + dateTimeParts[5];
                                // Mettre à jour la date et l'heure dans l'objet M_50592
                                m_50592.setDateFichier(java.sql.Date.valueOf(datePart));
                                m_50592.setHeureFichier(Time.valueOf(heurePart));
                            }
                        }

                        m50592Service.save(m_50592);


                    }

                    // Déplacer le fichier traité dans le dossier 'output'
                   // Path sourceFilePath = myClassFile.toPath();
                    //Path targetFilePath = outputFolder.toPath().resolve(myClassFile.getName());
                   // Files.move(sourceFilePath, targetFilePath);
                    //System.out.println("Le fichier " + myClassFile.getName() + " a été déplacé dans le dossier 'output'.");
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + myClassFile.getName() + " : " + e.getMessage());
                }
            }
        };
    }








}