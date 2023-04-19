package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.example.service.M_50592Service;

import org.example.service.MrService;
import org.example.service.SamService;
import org.example.service.TrainService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;

import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.io.*;


import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }



    @Bean
    CommandLineRunner runner(SamService samService, M_50592Service m50592Service , TrainService trainService, MrService mrService) {
        return args -> {

            Properties prop = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(input);

            String inputFolderPath = prop.getProperty("input.folder.path");
            String outputFolderPath = prop.getProperty("output.folder.path");


            File outputFolder = new File(outputFolderPath);
            File inputFolder = new File(inputFolderPath);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

//
//            String url = "https://test01.rd-vision-dev.com/get_images";
//            URL jsonUrl = new URL(url);
//
//            HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
//            connection.setRequestMethod("GET");
//
//// Ajouter le header Authorization avec le token
//            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";
//            connection.setRequestProperty("Authorization", "Bearer " + token);
//
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                InputStream inputStream = connection.getInputStream();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    response.append(line);
//                }
//                bufferedReader.close();
//                inputStream.close();
//
//                // Afficher la réponse
//                System.out.println("Response: " + response.toString());
//
//                // Parcourir l'objet JSON
//                JsonNode jsonNode = mapper.readTree(response.toString());
//                System.out.println("Train id: " + jsonNode.get("results").asText());
//                // Afficher d'autres valeurs du JSON en utilisant la méthode get() et asText()
//
//                connection.disconnect();
//            } else {
//                System.out.println("Error response code: " + connection.getResponseCode());
//            }















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


// Liste pour stocker les noms de fichiers train traités
            List<String> processedFiles = new ArrayList<>();
            // Lire tous les fichiers commençant par 'TRAIN'
            File[] trainFiles = outputFolder.listFiles((dir, name) -> name.startsWith("TRAIN") & name.endsWith(".json"));
            for (File trainFile : trainFiles) {
                String fileName = trainFile.getName();
                if (processedFiles.contains(fileName) || trainService.existsByfileName(trainFile.getName())) {
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
                        String urlt = outputFolderPath+"/"+ train.getFileName().substring(0, train.getFileName().lastIndexOf('.'));
                        train.setUrl(urlt);
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



            EnvloppeData enveloppeData = new EnvloppeData();
            // Lire tous les fichiers commençant par 'Sam'
            File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005") && name.endsWith(".json"));
            System.out.println(samFiles.length);
            for (File samFile : samFiles) {
                // Charger les enveloppes à partir du fichier JSON



                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {};
                try (InputStream samStream = new FileInputStream(samFile)) {
                    List<Sam> sams = mapper.readValue(samStream, samTypeRef);

                    for (Sam sam : sams) {



                                if (processedFilessam.contains(samFile.getName()) || samService.existsByfileName(samFile.getName())) {
                                    // Le fichier a déjà été traité, passer au suivant
                                    continue;
                                }
                                sam.checkOccultations();

                                sam.setFileName(samFile.getName()); // Définir le nom de fichier dans l'objet M_50592
                                sam.loadStartingWithSam(samFile.getName());
                                sam.loadSite(samFile.getName());
                                if (sam.getStatutSAM().equals("OK")) {
                                    sam.setUrlSam(null); // Définir l'URL à null
                                }





                        samService.save(sam);

                        processedFilessam.add(samFile.getName());
                        if (sam.getStatutSAM().equals("NOK")) {
                        for (int i = 1; i <= sam.getNbOccultations().size(); i++) {
                            System.out.println("nnoookkkk");


                                enveloppeData.loadFromJson(samFile, i);

                                // Créer un dossier avec le nom du fichier sans extension
                                File outputFolderenvloppe = new File(samFile.getParent(), samFile.getName().replace(".json", "") + "_enveloppes");
                                outputFolderenvloppe.mkdir();

                                // Créer le nom du fichier de sortie pour ce traitement spécifique
                                String outputFileName = samFile.getName().replace("SAM005", "SAMCapteur" + i);
                                File outputFile = new File(outputFolderenvloppe, outputFileName);

                                // Vérifier si le fichier de sortie existe déjà
                                if (!outputFile.exists()) {
                                    double step = 6.0; // step peut être changé selon vos besoins
                                    enveloppeData.saveSampledToJson(outputFile, step);
                                }
                                final int index = i;
                                // Vérifier si le fichier de sortie existe et si oui, le traiter
                                if (outputFile.exists()) {
                                    File[] samFilestraite = outputFolderenvloppe.listFiles((dir, name) -> name.startsWith("SAMCapteur" + index) && name.endsWith(".json"));

                                    for (File samFiletraite : samFilestraite) {
                                        enveloppeData.generateGraph(samFiletraite, i);
                                        System.out.println("voila le fichier " + samFiletraite);
                                    }
                                }
                                // Définir l'URL en fonction du nom de fichier
                                String urlsam = outputFolderenvloppe.getPath().replaceAll("\\\\", "/");

                                sam.setUrlSam(urlsam);



                            File[] imageFiles = outputFolder.listFiles((dir, name) -> name.contains(outputFolderenvloppe.getName().replace("_enveloppes", ""))
                                    && (name.endsWith(".png") || name.endsWith(".bmp")));
                            if (imageFiles.length > 0) {



                                // Déplacer les fichiers d'image correspondants dans le dossier créé
                                for (File imageFile : imageFiles) {
                                    File targetFile = new File(outputFolderenvloppe, imageFile.getName());
                                    if (!imageFile.renameTo(targetFile)) {
                                        System.err.println("Erreur lors du déplacement du fichier " + imageFile.getName() + " dans le dossier " + outputFolderenvloppe.getName() + ".");
                                    } else {
                                        System.out.println("Le fichier " + imageFile.getName() + " a été déplacé dans le dossier " + outputFolderenvloppe.getName() + ".");
                                    }
                                }
                            } else {
                                System.err.println("Aucun fichier d'image correspondant n'a été trouvé pour le dossier " + outputFolder + ".");
                            }




                            }




                        }
samService.save(sam);

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






                try (InputStream m50592Stream = new FileInputStream(m50592File)) {
                    List<M_50592> m_50592s = mapper.readValue(m50592Stream, m50592TypeRef);


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
                        if (m_50592.getBeR1().getxFond().contains("FF382A") || m_50592.getBeR1().getyFond().contains("FF382A") || m_50592.getBeR1().getzFond().contains("FF382A")|| m_50592.getBeR2().getxFond1().contains("FF382A") || m_50592.getBeR2().getyFond1().contains("FF382A") || m_50592.getBeR2().getzFond1().contains("FF382A") || m_50592.getBlR1().getxFondl().contains("FF382A") || m_50592.getBlR1().getyFondl().contains("FF382A")|| m_50592.getBlR1().getzFondl().contains("FF382A") || m_50592.getBlR2().getxFondl2().contains("FF382A") || m_50592.getBlR2().getyFondl2().contains("FF382A") || m_50592.getBlR2().getzFondl2().contains("FF382A")) {
                            m_50592.setStatut50592("NOK") ;
                        } else {
                            m_50592.setStatut50592("OK") ;
                        }
                        m50592Service.save(m_50592);
                        String url50592 = outputFolderPath+"/"+m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));

                        // Vérifier si le nom du fichier correspond au format JSON attendu
                        if (m50592File.getName().endsWith(".json")) {
                            System.out.println(m50592File.getParentFile());
                            // Extraire le nom du fichier JSON
                            String jsonFileName = m_50592.getFileName().substring(0, m50592File.getName().lastIndexOf('.'));
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


                                m_50592.setUrl50592("null");
                                m50592Service.save(m_50592);
                            } else {
                                System.err.println("Aucun fichier d'image correspondant n'a été trouvé pour le fichier JSON " + jsonFileName + ".");

                               m_50592.setUrl50592(url50592);
                                m50592Service.save(m_50592);

                            }
                        } else {
                            System.err.println("Le fichier " + m50592File.getName() + " ne correspond pas au format JSON attendu.");

                             m_50592.setUrl50592(url50592);
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