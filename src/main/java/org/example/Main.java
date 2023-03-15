package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.*;


import org.example.service.M_50592Service;

import org.example.service.MrService;
import org.example.service.SamService;
import org.example.service.TrainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }



    @Bean
    CommandLineRunner runner(SamService samService, M_50592Service m50592Service , TrainService trainService, MrService mrService) {
        return args -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            File inputFolder = new File("C:\\Users\\Ilham Barache\\Documents\\input");
            File outputFolder = new File("C:\\Users\\Ilham Barache\\Documents\\output");
            // Lire tous les fichiers commençant par 'TRAIN'
            File[] trainFiles = inputFolder.listFiles((dir, name) -> name.startsWith("TRAIN"));
            for (File trainFile : trainFiles) {
                TypeReference<List<Train>> trainTypeRef = new TypeReference<List<Train>>() {
                };
                try (InputStream trainStream = new FileInputStream(trainFile)) {
                    List<Train> trains = mapper.readValue(trainStream, trainTypeRef);
                    trainService.save(trains);
                    for (Train train : trains) {
                        train.setFileName(trainFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        train.loadStartingWithTRAIN(trainFile.getName());
                        train.loadSite(trainFile.getName());
                        String url = "C:\\Users\\Ilham Barache\\Documents\\output\\" + train.getFileName().substring(0, train.getFileName().lastIndexOf('.'));
                        train.setUrl(url);
                        trainService.save(train);
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + trainFile.getName() + " pour la table T_Passage : " + e.getMessage());
                }
            }



// Lire les fichiers Excel et mettre à jour les données des trains correspondants
            File[] excelFiles = inputFolder.listFiles((dir, name) -> name.endsWith(".xlsx"));
            for (File excelFile : excelFiles) {
                try (FileInputStream excelStream = new FileInputStream(excelFile)) {
                    Workbook workbook = new XSSFWorkbook(excelStream);
                    Sheet sheet = workbook.getSheetAt(0);
                    HashMap<String, Boolean> trainNums = new HashMap<>(); // HashMap pour stocker les numéros de train uniques
                    for (Row row : sheet) {
                        if (row.getRowNum() > 0) {
                            Cell numTrainCell = row.getCell(0);
                            String numTrain = null;
                            if (numTrainCell.getCellType() == CellType.STRING) {
                                numTrain = numTrainCell.getStringCellValue();
                            } else if (numTrainCell.getCellType() == CellType.NUMERIC) {
                                numTrain = String.valueOf((int) numTrainCell.getNumericCellValue());
                            }
                            if (!trainNums.containsKey(numTrain)) { // Vérifier si le numéro de train a déjà été traité
                                trainNums.put(numTrain, true); // Ajouter le numéro de train à la HashMap
                                Mr mr = new Mr();
                                mr.setMr(row.getCell(1).getStringCellValue());
                                mr.setNumTrain(numTrain);
                                mrService.save(mr);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + excelFile.getName() + " : " + e.getMessage());
                }
            }













            // Lire tous les fichiers commençant par 'Sam'
            File[] samFiles = inputFolder.listFiles((dir, name) -> name.startsWith("SAM"));
            for (File samFile : samFiles) {

                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {};

                 try (InputStream samStream = new FileInputStream(samFile)) {
                    List<Sam> sams = mapper.readValue(samStream, samTypeRef);
                    samService.save(sams);

                    for (Sam sam : sams) {

sam.checkOccultations();



                         sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        sam.loadStartingWithSam(samFile.getName());
                        sam.loadSite(samFile.getName());
                        if (sam.getStatut().equals("OK")) {
                            sam.setUrl(null); // Définir l'URL à null
                        } else {
                            // Définir l'URL en fonction du nom de fichier
                            String url = "C:\\Users\\Ilham Barache\\Documents\\output\\" + sam.getFileName().substring(0, sam.getFileName().lastIndexOf('.'));
                            sam.setUrl(url);
                        }


                          samService.save(sam);






                        // Récupération de l'instance de T_Passage correspondante en utilisant l'ID stocké dans l'objet Sam
                       // T_Passage passage = passageService.findById(sam.getPassage().getId());

                        // Affectation de l'instance de T_Passage à la propriété passage de l'objet Sam
                        //sam.setPassage(passage);

                        // Mise à jour de l'objet Sam dans la base de données



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
                        m_50592.loadSite(m50592File.getName());
                        Environnement env = m_50592.getEnvironnement();
                        String[] villes = env.extraireVilles(env.getSens());
                        if (villes != null) {
                            env.setVilleDepart(villes[0]);
                            env.setVilleArrivee(villes[1]);
                        }

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








        };





    }








}