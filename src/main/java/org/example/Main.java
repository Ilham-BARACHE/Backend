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


                        sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        sam.loadStartingWithSam(samFile.getName());
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
            for (File m50592File : m50592Files) {
                TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {
                };
                try (InputStream m50592Stream = new FileInputStream(m50592File)) {
                    List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);
                    m50592Service.save(m_50592s);
                    System.out.println("Le fichier " + m50592File.getName() + " a été traité avec succès !");


                    for (M_50592 m_50592 : m_50592s) {

                        m_50592.setFileName(m50592File.getName()); // Définir le nom de fichier dans l'objet M_50592
                        m_50592.loadStartingWith50592(m50592File.getName());


                        m50592Service.save(m_50592);


                    }




                    // Déplacer le fichier traité dans le dossier 'output'
                    // Path sourceFilePath = myClassFile.toPath();
                    //Path targetFilePath = outputFolder.toPath().resolve(myClassFile.getName());
                    // Files.move(sourceFilePath, targetFilePath);
                    //System.out.println("Le fichier " + myClassFile.getName() + " a été déplacé dans le dossier 'output'.");
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage());
                }
            }

            // Lire tous les fichiers commençant par 'TRAIN'
            File[] trainFiles = inputFolder.listFiles((dir, name) -> name.startsWith("TRAIN"));
            for (File trainFile : trainFiles) {
                TypeReference<List<Train>> trainTypeRef = new TypeReference<List<Train>>() {
                };
                try (InputStream trainStream = new FileInputStream(trainFile)) {
                    List<Train> trains = mapper.readValue(trainStream, trainTypeRef);
                    trainService.save(trains);
                    System.out.println("Le fichier " + trainFile.getName() + " a été traité avec succès !");


                    for (Train train : trains) {

                        train.setFileName(trainFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        train.loadStartingWithTrain(trainFile.getName());


                        trainService.save(train);


                    }




                    // Déplacer le fichier traité dans le dossier 'output'
                    // Path sourceFilePath = myClassFile.toPath();
                    //Path targetFilePath = outputFolder.toPath().resolve(myClassFile.getName());
                    // Files.move(sourceFilePath, targetFilePath);
                    //System.out.println("Le fichier " + myClassFile.getName() + " a été déplacé dans le dossier 'output'.");
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + trainFile.getName() + " : " + e.getMessage());
                }
            }





        };





    }








}