package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.component.Utils;
import org.example.model.M_50592;
import org.example.model.Mr;
import org.example.model.Sam;
import org.example.model.Train;
import org.example.repository.M_50592Repository;
import org.example.repository.MrRepository;
import org.example.repository.SamRepository;
import org.example.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@RestController

@CrossOrigin("http://localhost:3000/")
public class SamTrainController {


    @Autowired
    private Utils utils;


    private final SamRepository samRepository;


    private final TrainRepository trainRepository;

    private final MrRepository mrRepository;

    private final M_50592Repository m50592Repository;


    public SamTrainController(SamRepository samRepository, TrainRepository trainRepository, MrRepository mrRepository, M_50592Repository m50592Repository) {
        this.samRepository = samRepository;
        this.trainRepository = trainRepository;
        this.mrRepository = mrRepository;
        this.m50592Repository = m50592Repository;

    }

    private List<File> getFilesBySiteAndDateFichier50592(String site, Date dateFichier) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        ObjectMapper mapper = new ObjectMapper();
        String dateFichierStr = new SimpleDateFormat("yyyy.MM.dd").format(dateFichier);

        File[] m50592Files = outputFolder.listFiles((dir, name) -> name.startsWith("50592-" + site + "_" + dateFichierStr) && name.endsWith(".json"));

        if (m50592Files != null && m50592Files.length > 0) {
            return Arrays.asList(m50592Files);
        } else {
            return Collections.emptyList();
        }
    }


    @GetMapping("/parametreBL")
    public List<List<JsonNode>> getParametre(@RequestParam("site") String site, @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<File> files = getFilesBySiteAndDateFichier50592(site, dateFichier);
        List<List<JsonNode>> parametres = new ArrayList<>();

        if (!files.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : files) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode parametreNodes = rootNode.get("ParametresBL");

                List<JsonNode> parNodesSubList = new ArrayList<>();
                for (JsonNode par : parametreNodes) {
                    parNodesSubList.add(par);
                }

                parametres.add(parNodesSubList);
            }
        }

        return parametres;
    }

    @GetMapping("/parametreBE")
    public List<List<JsonNode>> getParametreBE(@RequestParam("site") String site, @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<File> files = getFilesBySiteAndDateFichier50592(site, dateFichier);
        List<List<JsonNode>> parametres = new ArrayList<>();

        if (!files.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : files) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode parametreNodes = rootNode.get("ParametresBE");

                List<JsonNode> parNodesSubList = new ArrayList<>();
                for (JsonNode par : parametreNodes) {
                    parNodesSubList.add(par);
                }

                parametres.add(parNodesSubList);
            }
        }

        return parametres;
    }




    private List<File> getFilesBySiteAndDateFichier(String site, Date dateFichier) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        ObjectMapper mapper = new ObjectMapper();
        String dateFichierStr = new SimpleDateFormat("yyyy.MM.dd").format(dateFichier);

        File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005-" + site + "_" + dateFichierStr) && name.endsWith(".json"));

        if (samFiles != null && samFiles.length > 0) {
            return Arrays.asList(samFiles);
        } else {
            return Collections.emptyList();
        }
    }

    @GetMapping("/temps")
    public List<List<JsonNode>> getTempsMs(@RequestParam("site") String site, @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<File> files = getFilesBySiteAndDateFichier(site, dateFichier);
        List<List<JsonNode>> tempsMsNodesList = new ArrayList<>();

        if (!files.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : files) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode tempsMsNodes = rootNode.get("Temps_ms");

                List<JsonNode> tempsMsNodesSubList = new ArrayList<>();
                for (JsonNode tempsMsNode : tempsMsNodes) {
                    tempsMsNodesSubList.add(tempsMsNode);
                }

                tempsMsNodesList.add(tempsMsNodesSubList);
            }
        }

        return tempsMsNodesList;
    }


    @GetMapping("/dataheure")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierHEURE(
            @RequestParam("site") String site,
            @RequestParam("heure") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Time heureTime = Time.valueOf(heure);
        List<Sam> sams = samRepository.findBySiteAndDateFichierAndHeureFichier(site, dateFichier , heureTime);

        List<List<JsonNode>> tempsMsNodesList = getTempsMs(site,date);

        List<Map<String, Object>> result = new ArrayList<>();

            Map<String, Object> trainMap = new HashMap<>();


            boolean foundSam = false;


            for (Sam sam : sams) {

                    List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
                    trainMap.put("tempsMs", tempsList);
                    foundSam = true;
                    break;
                }

        if (!foundSam) {
            trainMap.put("vitesse_moy", null);
            trainMap.put("NbEssieux", null);
            trainMap.put("urlSam", null);
            trainMap.put("statutSAM", null);
            trainMap.put("NbOccultations", null);
            trainMap.put("tempsMs", null);
        }







            result.add(trainMap);


        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }



    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichier(
            @RequestParam("site") String site,
            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);
        List<List<JsonNode>> tempsMsNodesList = getTempsMs(site,date);
        List< List<JsonNode>> parametres = getParametre(site,date);
        List< List<JsonNode>> parametresbe = getParametreBE(site,date);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Train train : trains) {
            Map<String, Object> trainMap = new HashMap<>();
            trainMap.put("numTrain", train.getNumTrain());
            trainMap.put("dateFichier", train.getDateFichier());
            trainMap.put("heureFichier", train.getHeureFichier());
            trainMap.put("url", train.getUrl());
            trainMap.put("site",site);

            boolean foundSam = false;
            boolean found50592 = false;

            for (Sam sam : sams) {
                if (train.getHeureFichier().equals(sam.getHeureFichier())) {
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    trainMap.put("NbEssieux", sam.getNbEssieux());
                    trainMap.put("urlSam", sam.getUrlSam());
                    trainMap.put("statutSAM", sam.getStatutSAM());
                    trainMap.put("NbOccultations", sam.getNbOccultations());
                    List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
                    trainMap.put("tempsMs", tempsList);
                    foundSam = true;
                    break;
                }
            }

            for (M_50592 m50592 : m50592s) {
                if (train.getHeureFichier().equals(m50592.getHeureFichier())) {
                    trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
                    trainMap.put("statut50592", m50592.getStatut50592());
                    trainMap.put("url50592", m50592.getUrl50592());
                    String concatenatedValuebe = m50592.getBeR1() + " " + m50592.getBeR2();
                    String concatenatedValuebl = m50592.getBlR1() + " " + m50592.getBlR2();
                    trainMap.put("be",concatenatedValuebe);

                    trainMap.put("bl",concatenatedValuebl);

                    List<JsonNode> par = parametres.get(m50592s.indexOf(m50592));
                    trainMap.put("parametrebl", par);
                    List<JsonNode> parbe = parametresbe.get(m50592s.indexOf(m50592));
                    trainMap.put("parametrebe", parbe);



                    found50592 = true;
                    break;
                }
            }

            if (!foundSam) {
                trainMap.put("vitesse_moy", null);
                trainMap.put("NbEssieux", null);
                trainMap.put("urlSam", null);
                trainMap.put("statutSAM", null);
                trainMap.put("NbOccultations", null);
                trainMap.put("tempsMs", null);
            }

            if (!found50592) {
                trainMap.put("meteo", null);
                trainMap.put("statut50592",null);
                trainMap.put("url50592", null);
                trainMap.put("BE_R1",null);
                trainMap.put("BE_R2",null);
                trainMap.put("BL_R1",null);
                trainMap.put("BL_R2",null);
            }

            Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
            if (mr != null) {
                trainMap.put("mr", mr.getMr());
            }
            result.add(trainMap);
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }




//    @GetMapping("/info")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierheure(
//            @RequestParam("site") String site,
//            @RequestParam("dateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
//
//        Date dateFichier = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        List<Sam> sams = samRepository.findBySiteAndDateFichier(site, dateFichier);
//        List<Train> trains = trainRepository.findBySiteAndDateFichier(site, dateFichier);
//        List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichier(site, dateFichier);
//
//        // Créer un Set pour stocker toutes les heures des enregistrements
//        Set<Date> heuresCommunes = new HashSet<>();
//        for (Sam sam : sams) {
//            heuresCommunes.add(sam.getHeureFichier());
//        }
//        for (Train train : trains) {
//            heuresCommunes.add(train.getHeureFichier());
//        }
//        for (M_50592 m50592 : m50592s) {
//            heuresCommunes.add(m50592.getHeureFichier());
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        // Itérer sur les heures communes et récupérer les enregistrements correspondants
//        for (Date heureCommune : heuresCommunes) {
//            List<Sam> samsHeureCommune = sams.stream()
//                    .filter(sam -> sam.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//            List<Train> trainsHeureCommune = trains.stream()
//                    .filter(train -> train.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//            List<M_50592> m50592sHeureCommune = m50592s.stream()
//                    .filter(m50592 -> m50592.getHeureFichier().equals(heureCommune))
//                    .collect(Collectors.toList());
//
//            // Ajouter les enregistrements correspondants dans la liste result
//            for (Sam sam : samsHeureCommune) {
//
//
//                ;for (M_50592 m50592 : m50592sHeureCommune) {
//
//
//                for (Train train : trainsHeureCommune) {
//                    Map<String, Object> trainMap = new HashMap<>();
//                    trainMap.put("id", train.getId());
//                    trainMap.put("numTrain", train.getNumTrain());
//
//
//                    if (sam != null) {
//                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                    } else {
//                        trainMap.put("vitesse_moy", null);
//                    }
//
//                    if (m50592 != null) {
//                        trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                    } else {
//                        trainMap.put("meteo", null);
//                    }
//
//                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
//                    if (mr != null) {
//                        trainMap.put("mr", mr.getMr());
//                    }
//
//                    result.add(trainMap);
//                }
//            }
//
//
//
//
//
//            }
//
//        }
//
//        return ResponseEntity.ok(result);
//
//
//    }

    private List<File> getFilesBySiteAndDateFichierBetween50592(String site, Date datestartFichier, Date datefinFichier) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        ObjectMapper mapper = new ObjectMapper();

        List<File> filesList = new ArrayList<>();

        File[] m50592Files = outputFolder.listFiles((dir, name) -> name.startsWith("50592-" + site + "_") && name.endsWith(".json"));
        if (m50592Files != null) {
            for (File m50592File : m50592Files) {
                String fileName = m50592File.getName();
                String[] parts = fileName.split("_");
                String fileDateStr = parts[1];

                try {
                    Date fileDate = new SimpleDateFormat("yyyy.MM.dd").parse(fileDateStr);

                    if (fileDate.compareTo(datestartFichier) >= 0 && fileDate.compareTo(datefinFichier) <= 0) {
                        filesList.add(m50592File);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(filesList);
        return filesList;
    }

    @GetMapping("/parametreblBetween")
    public List<List<JsonNode>> getparametreblBetween(
            @RequestParam("site") String site,
            @RequestParam("datestartFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datestart,
            @RequestParam("datefinFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datefin
    ) throws IOException {
        Date datestartFichier = Date.from(datestart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date datefinFichier = Date.from(datefin.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant());
        List<File> filesList = getFilesBySiteAndDateFichierBetween50592(site, datestartFichier, datefinFichier);

        List<List<JsonNode>> parametreMsList = new ArrayList<>();
        if (filesList != null) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : filesList) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode parametreMsNodes = rootNode.get("ParametresBL");
                List<JsonNode> fileparametreList = new ArrayList<>();

                for (JsonNode parametreMsNode : parametreMsNodes) {
                    fileparametreList.add(parametreMsNode);
                }
                parametreMsList.add(fileparametreList);
                System.out.println("Fichier : " + file.getName() + " BL : " + fileparametreList);
            }
        }
        System.out.println(parametreMsList);
        return parametreMsList;
    }


    @GetMapping("/parametrebeBetween")
    public List<List<JsonNode>> getparametrebeBetween(
            @RequestParam("site") String site,
            @RequestParam("datestartFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datestart,
            @RequestParam("datefinFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datefin
    ) throws IOException {
        Date datestartFichier = Date.from(datestart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date datefinFichier = Date.from(datefin.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant());
        List<File> filesList = getFilesBySiteAndDateFichierBetween50592(site, datestartFichier, datefinFichier);

        List<List<JsonNode>> parametreMsList = new ArrayList<>();
        if (filesList != null) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : filesList) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode parametreMsNodes = rootNode.get("ParametresBE");
                List<JsonNode> fileparametreList = new ArrayList<>();

                for (JsonNode parametreMsNode : parametreMsNodes) {
                    fileparametreList.add(parametreMsNode);
                }
                parametreMsList.add(fileparametreList);
                System.out.println("Fichier : " + file.getName() + " BE : " + fileparametreList);
            }
        }
        System.out.println(parametreMsList);
        return parametreMsList;
    }

    private List<File> getFilesBySiteAndDateFichierBetween(String site, Date datestartFichier, Date datefinFichier) throws IOException {
        Properties prop = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties");
        prop.load(input);

        String outputFolderPath = prop.getProperty("output.folder.path");

        File outputFolder = new File(outputFolderPath);
        ObjectMapper mapper = new ObjectMapper();

        List<File> filesList = new ArrayList<>();

        File[] samFiles = outputFolder.listFiles((dir, name) -> name.startsWith("SAM005-" + site + "_") && name.endsWith(".json"));
        if (samFiles != null) {
            for (File samFile : samFiles) {
                String fileName = samFile.getName();
                String[] parts = fileName.split("_");
                String fileDateStr = parts[1];

                try {
                    Date fileDate = new SimpleDateFormat("yyyy.MM.dd").parse(fileDateStr);

                    if (fileDate.compareTo(datestartFichier) >= 0 && fileDate.compareTo(datefinFichier) <= 0) {
                        filesList.add(samFile);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(filesList);
        return filesList;
    }



    @GetMapping("/tempsBetween")
    public List<List<JsonNode>> getTempsMsBetween(
            @RequestParam("site") String site,
            @RequestParam("datestartFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datestart,
            @RequestParam("datefinFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datefin
    ) throws IOException {
        Date datestartFichier = Date.from(datestart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date datefinFichier = Date.from(datefin.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant());
        List<File> filesList = getFilesBySiteAndDateFichierBetween(site, datestartFichier, datefinFichier);

        List<List<JsonNode>> tempsMsList = new ArrayList<>();
        if (filesList != null) {
            ObjectMapper mapper = new ObjectMapper();
            for (File file : filesList) {
                JsonNode rootNode = mapper.readTree(file);
                JsonNode tempsMsNodes = rootNode.get("Temps_ms");
                List<JsonNode> fileTempsMsList = new ArrayList<>();

                for (JsonNode tempsMsNode : tempsMsNodes) {
                    fileTempsMsList.add(tempsMsNode);
                }
                tempsMsList.add(fileTempsMsList);
                System.out.println("Fichier : " + file.getName() + " Temps_ms : " + fileTempsMsList);
            }
        }
        System.out.println(tempsMsList);
        return tempsMsList;
    }



    @GetMapping("/dataBetween")
public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierBetween(
        @RequestParam("site") String site,
        @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) throws Exception{


    Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
    List<M_50592> m50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
    List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<List<JsonNode>> tempsMsNodesList = getTempsMsBetween(site,startDate,endDate);
        List<List<JsonNode>> parametrebeNodesList = getparametrebeBetween(site,startDate,endDate);
        List<List<JsonNode>> parametreblNodesList = getparametreblBetween(site,startDate,endDate);
    List<Map<String, Object>> result = new ArrayList<>();
    for (Train train : trains) {
        Map<String, Object> trainMap = new HashMap<>();
        trainMap.put("numTrain", train.getNumTrain());
        trainMap.put("dateFichier", train.getDateFichier());
        trainMap.put("heureFichier", train.getHeureFichier());
        trainMap.put("url", train.getUrl());
        trainMap.put("site",site);

        boolean foundSam = false;
        boolean found50592 = false;

        for (Sam sam : sams) {
            if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
                trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                trainMap.put("datesam",sam.getDateFichier());
//                trainMap.put("NbEssieux", sam.getNbEssieux());
//                trainMap.put("urlSam", sam.getUrlSam());
//                trainMap.put("statutSAM", sam.getStatutSAM());
//                trainMap.put("NbOccultations", sam.getNbOccultations());
//                List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
//                trainMap.put("tempsMs", tempsList);


                foundSam = true;
                break;
            }
        }

        for (M_50592 m50592 : m50592s) {
            if (train.getHeureFichier().equals(m50592.getHeureFichier()) && train.getDateFichier().equals(m50592.getDateFichier())) {
//                trainMap.put("meteo", m50592.getEnvironnement().getMeteo());
//                trainMap.put("date50592",m50592.getDateFichier());
//                trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxSortie());
//                trainMap.put("statut50592", m50592.getStatut50592());
//                trainMap.put("url50592", m50592.getUrl50592());
//                trainMap.put("compteur",m50592.getEnvironnement().getCompteurEssieuxEntree());
//

                trainMap.put("ber1",m50592.getBlR1());







//                trainMap.put("bl",concatenatedValuebl);
//                List<JsonNode> parametrebl = parametreblNodesList.get(m50592s.indexOf(m50592));
//
//                trainMap.put("parametrebl",parametrebl);
//                List<JsonNode> parametrebe = parametrebeNodesList.get(m50592s.indexOf(m50592));
//
//                trainMap.put("parametrebe",parametrebe);


                found50592 = true;
                break;
            }
        }

        if (!foundSam) {
            trainMap.put("vitesse_moy", null);
            trainMap.put("NbEssieux", null);
            trainMap.put("urlSam", null);
            trainMap.put("statutSAM", null);
            trainMap.put("NbOccultations", null);
            trainMap.put("tempsMs", null);
        }

        if (!found50592) {
            trainMap.put("meteo", null);
            trainMap.put("statut50592",null);
            trainMap.put("url50592", null);
            trainMap.put("BE_R1",null);
            trainMap.put("BE_R2",null);
            trainMap.put("BL_R1",null);
            trainMap.put("BL_R2",null);
        }

        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
        if (mr != null) {
            trainMap.put("mr", mr.getMr());
        }
        result.add(trainMap);
    }

    if (result.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(result);
}


    @GetMapping("/dataBetweenDateSAM")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAMBetween(
            @RequestParam("site") String site,
            @RequestParam("statut") String statutSam,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception{


        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);

        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<List<JsonNode>> tempsMsNodesList = getTempsMsBetween(site,startDate,endDate);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Sam sam : sams) {
            if (!statutSam.equals("uniquement sam") && !sam.getStatutSAM().equals(statutSam)) {
                continue; // Si le statut de ce SAM n'est pas le statut demandé, on passe au prochain SAM
            }
            Map<String, Object> trainMap = null;
            for (Train train : trains) {
                if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
                    trainMap = new HashMap<>();
                    trainMap.put("numTrain", train.getNumTrain());
                    trainMap.put("dateFichier", train.getDateFichier());
                    trainMap.put("heureFichier", train.getHeureFichier());
                    trainMap.put("url", train.getUrl());
                    trainMap.put("vitesse_moy", sam.getVitesse_moy());
                    List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
                    trainMap.put("tempsMs", tempsList);

                    Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                    if (mr != null) {
                        trainMap.put("mr", mr.getMr());
                    }
                    result.add(trainMap);
                    break; // On a trouvé le train correspondant à ce SAM, on passe au prochain SAM
                }
            }
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }





    @GetMapping("/dataBetweenDatesam")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierSAM(
            @RequestParam("site") String site,
            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);

        if (sams.isEmpty() && trains.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Sam sam : sams) {
            if (sam.getStatutSAM().equals("NOK")) {
                for (Train train : trains) {
                    if (train.getDateFichier().equals(sam.getDateFichier()) &&
                            train.getHeureFichier().equals(sam.getHeureFichier())) {
                        Map<String, Object> trainMap = new HashMap<>();
                        trainMap.put("id", train.getId());
                        trainMap.put("numTrain", train.getNumTrain());
                        trainMap.put("dateFichier", train.getDateFichier());
                        trainMap.put("heureFichier", train.getHeureFichier());
                        trainMap.put("url", train.getStatut());



                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("id", sam.getId());
                        trainMap.put("NbEssieux", sam.getNbEssieux());
                        trainMap.put("url", sam.getUrlSam());
                        trainMap.put("statutSAM", sam.getStatutSAM());
                        trainMap.put("NbOccultations", sam.getNbOccultations());

                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                        if (mr != null) {
                            trainMap.put("mr", mr.getMr());
                        }

                        result.add(trainMap);
                        break;
                    }
                }
            }
        }

        return ResponseEntity.ok(result);
    }

    private int lastMonthOfQuarter(int month) {
        if (month <= 0 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        int quarter = (month - 1) / 3 + 1; // calcul du numéro de trimestre
        return quarter * 3; // le dernier mois du trimestre est le mois numéro 3, 6, 9 ou 12
    }

    @GetMapping("/dataQuarterlySAM")
    public ResponseEntity<List<Map<String, Object>>> getBySiteAndQuarterSAM(

    ) throws Exception {

        LocalDate startDate = LocalDate.of(2023, 1, 1);

        List<Map<String, Object>> result = new ArrayList<>();

        while (true) {
            LocalDate endDate = startDate.plusMonths(3);
            if (endDate.getYear() != startDate.getYear()) {
                endDate = endDate.withYear(startDate.getYear());
            }
            System.out.println("Trimestre " + ((endDate.getMonthValue() + 2) / 3) + " " + startDate + " - " + endDate.minusDays(1));


            int lastMonth = lastMonthOfQuarter(endDate.getMonthValue()); // on utilise endDate au lieu de startDate
            LocalDate endQuarterDate = LocalDate.of(endDate.getYear(), lastMonth, 30).with(TemporalAdjusters.lastDayOfMonth()); // on calcule la fin du trimestre
            System.out.println("Fin de trimestre: " + endQuarterDate);


            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<Train> trains = trainRepository.findAll();

            List<Sam> sams = samRepository.findAll();
//            List<List<JsonNode>> tempsMsNodesList = getTempsMsBetween(site, startDate, endDate);

            for (Sam sam : sams) {

                Map<String, Object> trainMap = null;
                for (Train train : trains) {
                    if (train.getHeureFichier().equals(sam.getHeureFichier()) && train.getDateFichier().equals(sam.getDateFichier())) {
                        trainMap = new HashMap<>();
                        trainMap.put("numTrain", train.getNumTrain());
                        trainMap.put("dateFichier", train.getDateFichier());
                        trainMap.put("heureFichier", train.getHeureFichier());
//                        trainMap.put("url", train.getUrl());
                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
                        trainMap.put("datesam" ,sam.getDateFichier());
//                        List<JsonNode> tempsList = tempsMsNodesList.get(sams.indexOf(sam));
//                        trainMap.put("tempsMs", tempsList);

                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
                        if (mr != null) {
                            trainMap.put("mr", mr.getMr());
                        }
                        result.add(trainMap);
                        break; // On a trouvé le train correspondant à ce SAM, on passe au prochain SAM
                    }
                }
            }

            if (endDate.isAfter(LocalDate.now())) {
                break;
            }
            startDate = endDate;
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }





//    @GetMapping("/dataBetweenDatesamNOK")
//    public ResponseEntity<List<Map<String, Object>>> getBySiteAndDateFichierNOK(
//            @RequestParam("site") String site,
//            @RequestParam("startDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam("FinDateFichier") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//    ) {
//        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//
//        List<Train> trains = trainRepository.findBySiteAndDateFichierBetween(site, start, end);
//        List<Sam> sams = samRepository.findBySiteAndDateFichierBetween(site, start, end);
//        List<M_50592> m_50592s = m50592Repository.findBySiteAndDateFichierBetween(site, start, end);
//
//        if (sams.isEmpty() && trains.isEmpty() && m_50592s.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Sam sam : sams) {
//            if (sam.getStatutSAM().equals("NOK")) {
//                for (Train train : trains) {
//                    if (train.getDateFichier().equals(sam.getDateFichier()) &&
//                            train.getHeureFichier().equals(sam.getHeureFichier())) {
//                        Map<String, Object> trainMap = new HashMap<>();
//                        trainMap.put("id", train.getId());
//                        trainMap.put("numTrain", train.getNumTrain());
//                        trainMap.put("dateFichier", train.getDateFichier());
//                        trainMap.put("heureFichier", train.getHeureFichier());
//                        trainMap.put("url", train.getStatut());
//
//
//
//                        trainMap.put("vitesse_moy", sam.getVitesse_moy());
//                        trainMap.put("id", sam.getId());
//                        trainMap.put("NbEssieux", sam.getNbEssieux());
//                        trainMap.put("url", sam.getUrlSam());
//                        trainMap.put("statutSAM", sam.getStatutSAM());
//                        trainMap.put("NbOccultations", sam.getNbOccultations());
//
//                        Mr mr = mrRepository.findByNumTrain(train.getNumTrain());
//                        if (mr != null) {
//                            trainMap.put("mr", mr.getMr());
//                        }
//
//                        result.add(trainMap);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return ResponseEntity.ok(result);
//    }

}