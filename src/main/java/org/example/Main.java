package org.example;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;


import org.example.model.*;
import org.example.repository.M_50592Repository;
import org.example.repository.ResultRepository;
import org.example.repository.SamRepository;
import org.example.repository.TrainRepository;
import org.example.service.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.*;


import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.ErrorManager;


@SpringBootApplication
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        logger.info("Le programme a démarré.");

        // Autres opérations de votre application

        logger.info("Le programme s'est terminé.");
    }





    private void deplacerFichiers(File[] files, File outputFolder) {

        for (File file : files) {
            File targetFile = new File(outputFolder, file.getName());
            if (targetFile.exists()) {
                logger.info("Le fichier cible existe déjà : " + targetFile.getAbsolutePath());
            } else {
                try {
                    Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Le fichier a été déplacé avec succès !");
                } catch (FileSystemException e) {
                    logger.info("Erreur lors du déplacement du fichier : " + e.getMessage());

                    // Tenter de déplacer le fichier à nouveau après une pause
                    int maxAttempts = 3; // Nombre maximal de tentatives de déplacement
                    int attempt = 0;

                    while (attempt < maxAttempts) {
                        attempt++;
                        try {
                            Thread.sleep(1000); // Pause de quelques millisecondes avant la prochaine tentative
                            Files.move(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Le fichier a été déplacé avec succès !");
                            break; // Sortir de la boucle si le déplacement réussit
                        } catch (IOException | InterruptedException ex) {
                            logger.info("Erreur lors du déplacement du fichier (tentative " + attempt + " sur " + maxAttempts + ") : " + ex.getMessage());
                        }
                    }

                    if (attempt == maxAttempts) {
                        logger.info("Impossible de déplacer le fichier après " + maxAttempts + " tentatives.");
                    }
                } catch (IOException e) {
                    logger.info("Erreur lors du déplacement du fichier : " + e.getMessage());
                }
            }
        }
        logger.info("Les fichiers ont été déplacés avec succès !");
    }









    @Bean
    CommandLineRunner runner(SamService samService, M_50592Service m50592Service , TrainService trainService, MrService mrService , SamRepository samRepository , M_50592Repository m50592Repository , TrainRepository trainRepository , ResultRepository resultRepository , ResultService resultService) {
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


            List<File> filesToMove = new ArrayList<>();

            // Créer une variable de contrôle pour arrêter la surveillance du répertoire
            AtomicBoolean isRunning = new AtomicBoolean(true);
            AtomicBoolean isWorking = new AtomicBoolean(true);


            // Créer un objet WatchService dans un thread séparé
            Thread watchThread = new Thread(() -> {
                try {
                    WatchService watchService = null;
                    try {
                        watchService = FileSystems.getDefault().newWatchService();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Path inputFolderPathh = inputFolder.toPath();
                    try {
                        inputFolderPathh.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    while (isRunning.get()) {
                        WatchKey key;
                        try {
                            key = watchService.take();
                        } catch (InterruptedException e) {
                            System.err.println("Erreur lors de la surveillance du répertoire : " + e.getMessage());
                            break;
                        }

                        // Parcourir les événements
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            // Vérifier si un nouveau fichier a été créé dans le répertoire "input"
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                                Path filePath = inputFolderPathh.resolve(pathEvent.context());

                                File newFile = filePath.toFile();
                                filesToMove.add(newFile);
                            }

                        }
                        deplacerFichiers(filesToMove.toArray(new File[0]), outputFolder);
                        logger.info("Les fichiers ont été déplacés avec succès !"+filesToMove);
                        filesToMove.clear(); // Vider la liste filesToMove
                        key.reset(); // Réinitialiser la clé pour continuer à surveiller les événements
                    }


                    } catch (RuntimeException e) {
                    logger.info("Erreur lors de la surveillance du répertoire : " + e.getMessage());
                }

            });
// Première partie du code pour déplacer les fichiers



                Thread processThread = new Thread(() -> {



                    while (isWorking.get()) {


// deuxieme partie du code pour le stockage des fichiers deplacé
// Liste pour stocker les numéros de train traités
                        List<String> processedTrainNumbers = new ArrayList<>();

// Lire les données de la base de données pour la comparaison avec les nouvelles données
                        List<Mr> allMrData = mrService.findAll();
                        for (Mr mr : allMrData) {
                            processedTrainNumbers.add(mr.getNumTrain());
                        }

// Lire les fichiers Excel et mettre à jour les données des trains correspondants
                        File[] excelFiles = outputFolder.listFiles((dir, name) -> name.endsWith(".xlsx"));
                        if (excelFiles != null) {
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
                        }

// Liste pour stocker les noms de fichiers traités sam
                        List<String> processedFilessam = new ArrayList<>();


                        EnvloppeData enveloppeData = new EnvloppeData();
                        // Lire tous les fichiers commençant par 'Sam'
                        File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005") && name.endsWith(".json"));
                        if (samFiles != null) {
                            for (File samFile : samFiles) {
                                // Charger les enveloppes à partir du fichier JSON


                                TypeReference<List<Sam>> samTypeRef = new TypeReference<List<Sam>>() {
                                };
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

                                                String urlsam = outputFolderenvloppe.getPath().replaceAll("\\\\", "/");

                                                sam.setUrlSam(urlsam);


                                            }


                                        }
                                        samService.save(sam);

                                    }


                                } catch (IOException e) {
                                    System.err.println("Erreur lors de la lecture du fichier " + samFile.getName() + " : " + e.getMessage());
                                }
                            }

                        }


// Liste pour stocker les noms de fichiers traités 50592
                        List<String> processedFiles50592 = new ArrayList<>();
                        // Lire tous les fichiers commençant par '50592'
                        File[] m50592Files = outputFolder.listFiles((dir, name) -> name.startsWith("50592") & name.endsWith(".json"));
                        if (m50592Files != null) {
                            for (File m50592File : m50592Files) {

                                String fileName = m50592File.getName();
                                if (processedFiles50592.contains(fileName) || m50592Service.existsByfileName(m50592File.getName())) {
                                    // Le fichier a déjà été traité, passer au suivant
                                    continue;
                                }
                                TypeReference<List<M_50592>> m50592TypeRef = new TypeReference<List<M_50592>>() {
                                };


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
                                        if (m_50592.getBeR1().getxFond().contains("FF382A") || m_50592.getBeR1().getyFond().contains("FF382A") || m_50592.getBeR1().getzFond().contains("FF382A") || m_50592.getBeR2().getxFond1().contains("FF382A") || m_50592.getBeR2().getyFond1().contains("FF382A") || m_50592.getBeR2().getzFond1().contains("FF382A") || m_50592.getBlR1().getxFondl().contains("FF382A") || m_50592.getBlR1().getyFondl().contains("FF382A") || m_50592.getBlR1().getzFondl().contains("FF382A") || m_50592.getBlR2().getxFondl2().contains("FF382A") || m_50592.getBlR2().getyFondl2().contains("FF382A") || m_50592.getBlR2().getzFondl2().contains("FF382A")) {
                                            m_50592.setStatut50592("NOK");
                                        } else {
                                            m_50592.setStatut50592("OK");
                                        }
                                        m50592Service.save(m_50592);

                                        System.out.println("je suis ici dans 50592");
                                        String jsonFileName = m_50592.getFileName().substring(0, m_50592.getFileName().lastIndexOf('.'));

                                        File outputFolderFile = new File(outputFolder, jsonFileName);
                                        System.out.println("voila dossier crée" + outputFolderFile.getName());
                                        String url50592 = outputFolderFile.getAbsolutePath().replace("\\", "/");


                                        // Vérifier si le nom du fichier image correspondant contient le nom du fichier JSON
                                        File[] imageFiles = outputFolder.listFiles((dir, name) -> name.contains(outputFolderFile.getName().substring(0, m_50592.getFileName().lastIndexOf('_')))
                                                && (name.endsWith(".png") || name.endsWith(".bmp")));
                                        if (imageFiles.length > 0) {


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

                                        m_50592.setUrl50592(url50592);
                                        m50592Service.save(m_50592);

                                    }

                                    processedFiles50592.add(fileName);


                                } catch (IOException e) {
                                    System.err.println("Erreur lors de la lecture du fichier " + m50592File.getName() + " : " + e.getMessage());
                                }
                            }

                        }


                        // Construire l'URL de train en utilisant la date et l'heure
                        List<Sam> sams = samRepository.findAll();
                        List<M_50592> m50592s = m50592Repository.findAll();
                        DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        Set<String> existingResultIds = new HashSet<>();


                        for (Sam sam : sams) {
                            for (M_50592 m50592 : m50592s) {
                                if (sam.getDateFichier().equals(m50592.getDateFichier())) {
                                    String url = "https://test01.rd-vision-dev.com/get_images?system=2&dateFrom=" +
                                            sam.getDateFichier() + "T" + sam.getHeureFichier() +
                                            "&dateTo=" + m50592.getDateFichier() + "T" + m50592.getHeureFichier();

                                    URL jsonUrl = null;
                                    try {
                                        jsonUrl = new URL(url);
                                    } catch (MalformedURLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    HttpURLConnection connection = null;
                                    try {
                                        connection = (HttpURLConnection) jsonUrl.openConnection();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    try {
                                        connection.setRequestMethod("GET");
                                    } catch (ProtocolException e) {
                                        throw new RuntimeException(e);
                                    }

                                    // Ajouter le header Authorization avec le token
                                    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoidGVzdCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2VtYWlsYWRkcmVzcyI6InRlc3QudXNlckB0ZXN0LmNvbSIsImV4cCI6MTY5NjYwMDY5MiwiaXNzIjoiand0dGVzdC5jb20iLCJhdWQiOiJ0cnlzdGFud2lsY29jay5jb20ifQ.LQ6yfa0InJi6N5GjRfVcA8XMZtZZef0PswrM2Io7l-g";
                                    connection.setRequestProperty("Authorization", "Bearer " + token);

                                    try {
                                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            InputStream inputStream = connection.getInputStream();
                                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                            StringBuilder response = new StringBuilder();
                                            String line;
                                            while ((line = bufferedReader.readLine()) != null) {
                                                response.append(line);
                                            }
                                            bufferedReader.close();
                                            inputStream.close();

                                            // Mapper le JSON sur un objet Train
                                            Train train = mapper.readValue(response.toString(), Train.class);

                                            List<Result> results = train.getResults();
                                            int size = results.size();

                                            for (int i = 0; i < size; i++) {
                                                Result result = results.get(i);
                                                String dateid = result.getDate();


                                                // Effectuez une vérification pour déterminer si l'ID du résultat existe déjà
                                                if (existingResultIds.contains(dateid)) {
                                                    // Le résultat existe déjà, passez à l'itération suivante
                                                    continue;
                                                }

                                                String dateTimeString = dateid.substring(0, 19);
                                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatterr);
                                                Date formattedDateTime = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

                                                // extraire la date et la convertir en java.util.Date
                                                String[] parts = dateTimeString.split("T");
                                                String datePart = parts[0]; // "2023-04-14"
                                                String timePart = parts[1]; // "14:04:05"

                                                SimpleDateFormat dateFormatterr = new SimpleDateFormat("yyyy-MM-dd");
                                                Date datefichier = dateFormatterr.parse(datePart);

                                                SimpleDateFormat timeFormatterr = new SimpleDateFormat("HH:mm:ss");
                                                Date timefichier = timeFormatterr.parse(timePart);

                                                // Ajoutez l'ID du résultat à la liste des résultats existants
                                                existingResultIds.add(dateid);

                                                // Convertir les objets Date en objets Time
                                                Time heurefichier = new Time(timefichier.getTime());

                                                // Vérifier si une instance de Train avec la même date, heure et site existe déjà
                                                List<Train> existingTrain = trainRepository.findBySiteAndDateFichierAndHeureFichier("Chevilly", datefichier, heurefichier);
                                                if (!existingTrain.isEmpty()) {
                                                    // Une instance de Train avec la même date, heure et site existe déjà, passez à l'itération suivante
                                                    continue;
                                                }


                                                Train trainInstance = new Train(); // Créer une nouvelle instance de Train
                                                trainInstance.setDateFichier(datefichier);
                                                trainInstance.setHeureFichier(timefichier);
                                                trainInstance.setSite("Chevilly");

                                                result.setTrain(trainInstance); // Définir la relation train dans Result


                                                trainInstance.getResults().add(result);
                                                System.out.println("je jee " + trainInstance.getSite());

                                                trainService.save(trainInstance); // Sauvegarder chaque instance de Train séparément
                                                resultService.save(result); // Sauvegarder chaque instance de Result séparément
                                            }
                                        } else {
                                            System.out.println("Error response code: " + connection.getResponseCode());
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }

                                    connection.disconnect();
                                }
                            }
                        }

                        // Pause de quelques secondes entre chaque exécution
                        try {
                            Thread.sleep(5000); // Réglage de la durée de pause selon vos besoins
                        } catch (InterruptedException e) {
                            System.err.println("Le thread de traitement a été interrompu : " + e.getMessage());
                            break;
                        }


                    }
                });


// Démarrer les threads de surveillance et de traitement
                watchThread.start();
                processThread.start();


// Attendre que les deux threads aient terminé leur exécution
            try {
                watchThread.join();
                processThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        };





    }








}