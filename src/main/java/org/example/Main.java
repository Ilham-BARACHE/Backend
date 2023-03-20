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
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
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



            File[] files = inputFolder.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".json") || file.getName().endsWith(".xlsx")) {
                    try {
                        Thread.sleep(1000); // Attendre 5 secondes avant de déplacer le fichier dans le dossier output

                        File targetFile = new File(outputFolder, file.getName());
                        if (targetFile.exists()) {
                            System.err.println("Le fichier cible existe déjà : " + targetFile.getAbsolutePath());
                        } else {
                            try {
                                Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Le fichier a été déplacé avec succès !");
                            } catch (IOException e) {
                                System.err.println("Erreur lors du déplacement du fichier : " + e.getMessage());
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

// Liste pour stocker les noms de fichiers traités
            List<String> processedFiles = new ArrayList<>();
            // Lire tous les fichiers commençant par 'TRAIN'
            File[] trainFiles = outputFolder.listFiles((dir, name) -> name.startsWith("TRAIN") & name.endsWith(".json"));
            for (File trainFile : trainFiles) {
                String fileName = trainFile.getName();
                if (processedFiles.contains(fileName)|| trainService.existsByfileName(trainFile.getName()) ){
                    // Le fichier a déjà été traité, passer au suivant
                    continue;
                }
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
                    processedFiles.add(fileName);
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + trainFile.getName() + " pour la table T_Passage : " + e.getMessage());
                }
            }



// Liste pour stocker les numéros de train traités
            List<String> processedTrainNumbers = new ArrayList<>();

// Lire les données de la base de données pour la comparaison avec les nouvelles données
            List<Mr> allMrData = mrService.findAll();
            for (Mr mr : allMrData) {
                processedTrainNumbers.add(mr.getNumTrain());
            }

// Lire les fichiers Excel et mettre à jour les données des trains correspondants
            File[] excelFiles = outputFolder.listFiles((dir, name) -> name.endsWith(".xlsx"));
            for (File excelFile : excelFiles) {
                try (FileInputStream excelStream = new FileInputStream(excelFile)) {
                    Workbook workbook = new XSSFWorkbook(excelStream);
                    Sheet sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        if (row.getRowNum() > 0) {
                            Cell numTrainCell = row.getCell(0);
                            String numTrain = null;
                            if (numTrainCell.getCellType() == CellType.STRING) {
                                numTrain = numTrainCell.getStringCellValue();
                            } else if (numTrainCell.getCellType() == CellType.NUMERIC) {
                                numTrain = String.valueOf((int) numTrainCell.getNumericCellValue());
                            }
                            String mr = row.getCell(1).getStringCellValue();
                            if (!processedTrainNumbers.contains(numTrain)) {
                                // Vérifier si le numéro de train a déjà été traité
                                // Si le numéro de train n'a pas encore été traité, ajouter une nouvelle entrée dans la base de données
                                Mr newMr = new Mr();
                                newMr.setMr(mr);
                                newMr.setNumTrain(numTrain);
                                mrService.save(newMr);
                                // Ajouter le numéro de train traité à la liste des numéros de train traités
                                processedTrainNumbers.add(numTrain);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }








// Liste pour stocker les noms de fichiers traités
            List<String> processedFilessam = new ArrayList<>();
           // Lire tous les fichiers commençant par 'Sam'
            File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM") & name.endsWith(".json"));
            for (File samFile : samFiles) {

                if (processedFilessam.contains(samFile.getName()) || samService.existsByfileName(samFile.getName())) {
                    // Le fichier a déjà été traité, passer au suivant
                    continue;
                }

                System.out.println(processedFilessam);
                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {};
                try (InputStream samStream = new FileInputStream(samFile)) {
                    List<Sam> sams = mapper.readValue(samStream, samTypeRef);

                    for (Sam sam : sams) {
                        sam.checkOccultations();

                        sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        sam.loadStartingWithSam(samFile.getName());
                        sam.loadSite(samFile.getName());
                        if (sam.getStatutSAM().equals("OK")) {
                            sam.setUrlSam(null); // Définir l'URL à null
                        } else {
                            // Définir l'URL en fonction du nom de fichier
                            String url = "C:\\Users\\Ilham Barache\\Documents\\output\\" + sam.getFileName().substring(0, sam.getFileName().lastIndexOf('.'));
                            sam.setUrlSam(url);
                        }


                        samService.save(sam);

                        processedFilessam.add(samFile.getName());



                        // Vérifier si le nom du fichier correspond au format JSON attendu
                        if (samFile.getName().endsWith(".json")) {
                            // Extraire le nom du fichier JSON
                            String jsonFileName = samFile.getName().substring(0, samFile.getName().lastIndexOf('.'));
                            // Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON
                            File[] imageFiles = inputFolder.listFiles((dir, name) -> name.contains(jsonFileName)
                                    && (name.endsWith(".png") || name.endsWith(".bmp")));
                            if (imageFiles.length > 0) {
                                // Créer le dossier correspondant au nom du fichier JSON
                                File outputFolderFile = new File(outputFolder, jsonFileName);
                                if (!outputFolderFile.exists() && !outputFolderFile.mkdir()) {
                                    System.err.println("Erreur lors de la création du dossier " + jsonFileName + ".");
                                } else {
                                    System.out.println("Le dossier " + jsonFileName + " a été créé.");
                                }

                                // Déplacer les fichiers d'image correspondants dans le dossier créé
                                for (File imageFile : imageFiles) {
                                    File targetFile = new File(outputFolderFile, imageFile.getName());
                                    if (!imageFile.renameTo(targetFile)) {
                                        System.err.println("Erreur lors du déplacement du fichier " + imageFile.getName() + " dans le dossier " + jsonFileName + ".");
                                    } else {
                                        System.out.println("Le fichier " + imageFile.getName() + " a été déplacé dans le dossier " + jsonFileName + ".");
                                    }
                                }
                            } else {
                                System.err.println("Aucun fichier d'image correspondant n'a été trouvé pour le fichier JSON " + jsonFileName + ".");
                            }
                        } else {
                            System.err.println("Le fichier " + samFile.getName() + " ne correspond pas au format JSON attendu.");
                        }








                    }



                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage());
                }
            }



// Liste pour stocker les noms de fichiers traités
            List<String> processedFiles50592 = new ArrayList<>();
            // Lire tous les fichiers commençant par '50592'
            File[] m50592Files = outputFolder.listFiles((dir, name) -> name.startsWith("50592") & name.endsWith(".json"));
            for (File m50592File : m50592Files) {
                String fileName = m50592File.getName();
                if (processedFiles50592.contains(fileName) ||   m50592Service.existsByfileName(m50592File.getName())) {
                    // Le fichier a déjà été traité, passer au suivant
                    continue;
                }
                TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {  };

                try {
                    Thread.sleep(5000); // Attendre 5 secondes avant de déplacer le fichier dans le dossier output
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Déplacer le fichier SAM dans le dossier output
                File outputFile = new File(outputFolder, m50592File.getName());
                if (!m50592File.renameTo(outputFile)) {
                    System.err.println("Erreur lors du déplacement du fichier " + m50592File.getName() + " dans le dossier output.");
                    continue; // Passer au fichier suivant en cas d'erreur
                }


                try (InputStream m50592Stream = new FileInputStream(outputFile)) {
                    List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);


                    for (M_50592 m_50592 : m_50592s) {

                        m_50592.setFileName(outputFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                        m_50592.loadStartingWith50592(outputFile.getName());
                        m_50592.loadSite(outputFile.getName());
                        Environnement env = m_50592.getEnvironnement();
                        String[] villes = env.extraireVilles(env.getSens());
                        if (villes != null) {
                            env.setVilleDepart(villes[0]);
                            env.setVilleArrivee(villes[1]);
                        }

                        m50592Service.save(m_50592);
                        // Vérifier si le nom du fichier correspond au format JSON attendu
                        if (m50592File.getName().endsWith(".json")) {
                            // Extraire le nom du fichier JSON
                            String jsonFileName = m50592File.getName().substring(0, m50592File.getName().lastIndexOf('.'));
                            // Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON
                            File[] imageFiles = inputFolder.listFiles((dir, name) -> name.contains(jsonFileName)
                                    && (name.endsWith(".png") || name.endsWith(".bmp")));
                            if (imageFiles.length > 0) {
                                // Créer le dossier correspondant au nom du fichier JSON
                                File outputFolderFile = new File(outputFolder, jsonFileName);
                                if (!outputFolderFile.exists() && !outputFolderFile.mkdir()) {
                                    System.err.println("Erreur lors de la création du dossier " + jsonFileName + ".");
                                } else {
                                    System.out.println("Le dossier " + jsonFileName + " a été créé.");
                                }

                                // Déplacer les fichiers d'image correspondants dans le dossier créé
                                for (File imageFile : imageFiles) {
                                    File targetFile = new File(outputFolderFile, imageFile.getName());
                                    if (!imageFile.renameTo(targetFile)) {
                                        System.err.println("Erreur lors du déplacement du fichier " + imageFile.getName() + " dans le dossier " + jsonFileName + ".");
                                    } else {
                                        System.out.println("Le fichier " + imageFile.getName() + " a été déplacé dans le dossier " + jsonFileName + ".");
                                    }
                                }

                                m_50592.setStatut50592("OK");
                                m_50592.setUrl50592("null");
                                m50592Service.save(m_50592);
                            } else {
                                System.err.println("Aucun fichier d'image correspondant n'a été trouvé pour le fichier JSON " + jsonFileName + ".");
                                m_50592.setStatut50592("NOK");
                                String url = "C:\\Users\\Ilham Barache\\Documents\\output\\" + m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));
                                m_50592.setUrl50592(url);
                                m50592Service.save(m_50592);

                            }
                        } else {
                            System.err.println("Le fichier " + m50592File.getName() + " ne correspond pas au format JSON attendu.");
                            m_50592.setStatut50592("NOK");
                            String url = "C:\\Users\\Ilham Barache\\Documents\\output\\" + m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));
                            m_50592.setUrl50592(url);
                            m50592Service.save(m_50592);
                        }


                    }

processedFiles50592.add(fileName);


                        } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage());
                }
            }








        };





    }








}