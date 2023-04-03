//package org.example.model;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.annotations.XYTextAnnotation;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.NumberTickUnit;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.RectangleInsets;
//import org.jfree.ui.TextAnchor;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.geom.Ellipse2D;
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.util.*;
//import java.util.List;
//
//public class EnvloppeData {
//
//    private List<Double> x;
//    private List<Double> y;
//    private double dtMs;
//    private List<Double> x1;
//    private List<Double> y1;
//
//    private List<Double> x2;
//    private List<Double> y2;
//
//    private List<Double> x3;
//    private List<Double> y3;
//    private List<Double> x4;
//    private List<Double> y4;
//    private List<Double> x5;
//    private List<Double> y5;
//    private List<Double> x6;
//    private List<Double> y6;
//    private List<Double> x7;
//    private List<Double> y7;
//
//    public List<Double> getX() {
//        return x;
//    }
//
//    public List<Double> getY() {
//        return y;
//    }
//
//
//    public double getDtMs() {
//        return dtMs;
//    }
//
//    public void setDtMs(double dtMs) {
//        this.dtMs = dtMs;
//    }
//
//    private double[][] bornes;
//    private double TempsMin;
//    private double TempsMax;
//
//    public EnvloppeData() {
//        x = new ArrayList<>();
//        y = new ArrayList<>();
//        x1 = new ArrayList<>();
//        y1 = new ArrayList<>();
//
//        x2 = new ArrayList<>();
//        y2 = new ArrayList<>();
//        x3 = new ArrayList<>();
//        y3 = new ArrayList<>();
//
//        x4 = new ArrayList<>();
//        y4 = new ArrayList<>();
//        x5 = new ArrayList<>();
//        y5 = new ArrayList<>();
//
//        x6 = new ArrayList<>();
//        y6 = new ArrayList<>();
//        x7 = new ArrayList<>();
//        y7 = new ArrayList<>();
//        dtMs = 0.0;
//
//    }
//
//    public void loadFromJson(File jsonFile) throws IOException {
//
//
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(jsonFile);
//            JsonNode enveloppeNode = rootNode.get("Enveloppes");
//
//
//        if (enveloppeNode != null) {
//            dtMs = enveloppeNode.get("dt_ms").asDouble();
//
//
//// Récupérer les données de Capteurs[0]
//            JsonNode capteursNode = enveloppeNode.get("Capteurs").get(0);
//            JsonNode xNode = capteursNode.get("X");
//
//            JsonNode yNode = capteursNode.get("Y");
//
//            for (int i = 0; i < xNode.size(); i++) {
//                x.add(xNode.get(i).asDouble());
//                y.add(yNode.get(i).asDouble());
//            }
//// Récupérer les données de Capteurs[1]
//            JsonNode capteursNode1 = enveloppeNode.get("Capteurs").get(1);
//            JsonNode xNode1 = capteursNode1.get("X");
//            JsonNode yNode1 = capteursNode1.get("Y");
//            for (int i = 0; i < xNode1.size(); i++) {
//                x1.add(xNode1.get(i).asDouble());
//                y1.add(yNode1.get(i).asDouble());
//            }
//
//            // Récupérer les données de Capteurs[2]
//            JsonNode capteursNode2 = enveloppeNode.get("Capteurs").get(2);
//            JsonNode xNode2 = capteursNode2.get("X");
//            JsonNode yNode2 = capteursNode2.get("Y");
//            for (int i = 0; i < xNode2.size(); i++) {
//                x2.add(xNode2.get(i).asDouble());
//                y2.add(yNode2.get(i).asDouble());
//            }
//
//
//            // Récupérer les données de Capteurs[1]
//            JsonNode capteursNode3 = enveloppeNode.get("Capteurs").get(3);
//            JsonNode xNode3 = capteursNode3.get("X");
//            JsonNode yNode3 = capteursNode3.get("Y");
//            for (int i = 0; i < xNode3.size(); i++) {
//                x3.add(xNode3.get(i).asDouble());
//                y3.add(yNode3.get(i).asDouble());
//            }
//
//
//            // Récupérer les données de Capteurs[1]
//            JsonNode capteursNode4 = enveloppeNode.get("Capteurs").get(4);
//            JsonNode xNode4 = capteursNode4.get("X");
//            JsonNode yNode4 = capteursNode4.get("Y");
//            for (int i = 0; i < xNode4.size(); i++) {
//                x4.add(xNode4.get(i).asDouble());
//                y4.add(yNode4.get(i).asDouble());
//            }
//
//
//            // Récupérer les données de Capteurs[1]
//            JsonNode capteursNode5 = enveloppeNode.get("Capteurs").get(5);
//            JsonNode xNode5 = capteursNode5.get("X");
//            JsonNode yNode5 = capteursNode5.get("Y");
//            for (int i = 0; i < xNode5.size(); i++) {
//                x5.add(xNode5.get(i).asDouble());
//                y5.add(yNode5.get(i).asDouble());
//            }
//
//
//            // Récupérer les données de Capteurs[1]
//            JsonNode capteursNode6 = enveloppeNode.get("Capteurs").get(6);
//            JsonNode xNode6 = capteursNode6.get("X");
//            JsonNode yNode6 = capteursNode6.get("Y");
//            for (int i = 0; i < xNode6.size(); i++) {
//                x6.add(xNode6.get(i).asDouble());
//                y6.add(yNode6.get(i).asDouble());
//            }
//
//
//            // Récupérer les données de Capteurs[1]
//            JsonNode capteursNode7 = enveloppeNode.get("Capteurs").get(7);
//            JsonNode xNode7 = capteursNode7.get("X");
//            JsonNode yNode7 = capteursNode7.get("Y");
//            for (int i = 0; i < xNode7.size(); i++) {
//                x7.add(xNode7.get(i).asDouble());
//                y7.add(yNode7.get(i).asDouble());
//            }
//        }
//    }
//
//
//    //calcule les bornes inférieures et supérieures des données enregistrées en x et y
//    public void CalculerBornes() {
//
//
//
//        double borneInfX = Collections.min(x);
//
//        double borneSupX = Collections.max(x);
//        String borneSupXString = String.valueOf((long) borneSupX);
//        double borneSupXValue = Double.parseDouble(borneSupXString);
//
//
//
//
//        double borneInfY = Collections.min(y);
//        double borneSupY = Collections.max(y);
//
//        this.bornes = new double[][] { {borneInfX, borneInfY}, {borneSupXValue, borneSupY} };
//        this.TempsMin = bornes[0][0];
//        this.TempsMax = bornes[1][0];
//    }
//
//    //permet de garder uniquement une partie des données qui se situent entre deux bornes de temps spécifiées
//    public void GarderSegment(double tempsMin, double tempsMax) {
//        List<Double> newX = new ArrayList<>();
//        List<Double> newY = new ArrayList<>();
//        for (int i = 0; i < x.size(); i++) {
//            if (x.get(i) >= tempsMin && x.get(i) <= tempsMax) {
//                newX.add(x.get(i));
//                newY.add(y.get(i));
//            }
//        }
//        x = newX;
//        y = newY;
//        // ajuste les bornes pour contenir toutes les données
//        if (TempsMin < tempsMin) {
//            TempsMin = tempsMin;
//        }
//        if (TempsMax > tempsMax) {
//            TempsMax = tempsMax;
//        }
//
//    }
//
//    //méthode principale qui échantillonne les données. Elle calcule d'abord les bornes de temps et les ajuste pour contenir toutes les données. Ensuite, elle trouve les indices du début et de la fin de chaque "segment" de données (où la valeur de y est inférieure à 0,2) et calcule les bornes correspondantes en x. Enfin, elle échantillonne les données en fonction d'un pas donné (step) pour obtenir un nombre fixe de points échantillonnés
//    public double[][] sample(double step) {
//
//
//        List<Double> lsttmpmin = new ArrayList<Double>();
//        List<Double> lsttmpmax = new ArrayList<Double>();
//        EnvloppeData enveloppeData = this;
//
//
//        enveloppeData.CalculerBornes();
//
//        lsttmpmin.add(enveloppeData.TempsMin);
//        lsttmpmax.add(enveloppeData.TempsMax);
//
//        double tempsMin = (Math.floor(Collections.min(lsttmpmin) / 10000) - 1) * 10000d;
//        double tempsMax = (Math.ceil(Collections.max(lsttmpmax) / 10000) + 1) * 10000d;
//
//
//        enveloppeData.GarderSegment(tempsMin, tempsMax);
//
//        // Pour chaque capteur, trouver la première et la dernière valeur en dessous de 0.2 dans le tableau Y.
//        List<Integer> firstIndices = new ArrayList<Integer>();
//        List<Integer> lastIndices = new ArrayList<Integer>();
//        double minY = 0.25;
//        for (int i = 0; i < y.size(); i++) {
//            if (y.get(i) < minY) {
//                if (firstIndices.isEmpty()) {
//                    firstIndices.add(i);
//                }
//                lastIndices.clear();
//                lastIndices.add(i);
//            } else {
//                if (!lastIndices.isEmpty()) {
//                    firstIndices.add(lastIndices.get(0));
//                    lastIndices.clear();
//                }
//            }
//        }
//
//        // Déterminer la valeur minimale de toutes les premières valeurs (XP) et la valeur maximale de toutes les dernières valeurs (XD).
//        double xp = Double.MAX_VALUE;
//        double xd = Double.MIN_VALUE;
//        for (int i = 0; i < lastIndices.size(); i++) {
//            int lastIndex = lastIndices.get(i);
//            int firstIndex = firstIndices.get(i);
//            if (i > 0) {
//                firstIndex = lastIndices.get(i - 1) ;
//            }
//            double xFirst = x.get(firstIndex);
//            double xLast = x.get(lastIndex);
//            xp = Math.min(xp, xFirst);
//            xd = Math.max(xd, xLast);
//        }
//
//
//
//
//
//        // Calculer le nombre de valeurs entre XP et XD et diviser ce nombre par la largeur maximale des images souhaitée (par exemple, 6000).
//        long numSamples = (long) Math.ceil( (xd - xp) / step);
//
//
//        int maxSamples = 6000;
//        if (numSamples <= maxSamples) {
//            return sampleAllData();
//        } else {
//            return sampleDataWithFixedWidth(xp, xd, maxSamples);
//        }
//
//    }
//
//    //échantillonne toutes les données à intervalles réguliers
//    private double[][] sampleAllData() {
//        double[][] sampledData = new double[2][x.size()];
//        for (int i = 0; i < x.size(); i++) {
//            sampledData[0][i] = x.get(i);
//            sampledData[1][i] = y.get(i);
//        }
//        return sampledData;
//    }
//
//    //échantillonne les données en conservant une largeur fixe pour l'ensemble des données échantillonnées
//    private double[][] sampleDataWithFixedWidth(double xp, double xd, int numSamples) {
//        double step = (xd - xp) / (numSamples - 1);
//        double[][] sampledData = new double[2][numSamples];
//        for (int i = 0; i < numSamples; i++) {
//            double xSampled = xp + i * step;
//            double ySampled = interpolateY(xSampled);
//            sampledData[0][i] = xSampled;
//            sampledData[1][i] = ySampled;
//        }
//        return sampledData;
//    }
//
////interpole une valeur de y en fonction d'une valeur donnée de x en utilisant une approximation linéaire entre les deux points les plus proches.
//    private double interpolateY(double xSampled) {
//        int i = 0;
//        while (i < x.size() - 1 && x.get(i) < xSampled) {
//            i++;
//        }
//        if (i == 0) {
//            return y.get(0);
//        } else if (i == x.size()) {
//            return y.get(x.size() - 1);
//        } else {
//            double x1 = x.get(i - 1);
//            double x2 = x.get(i);
//            double y1 = y.get(i - 1);
//            double y2 = y.get(i);
//            double slope = (y2 - y1) / (x2 - x1);
//            double ySampled = y1 + slope * (xSampled - x1);
//            return ySampled;
//        }
//    }
//    //échantillonne les données et les sauvegarde au format JSON dans un fichier spécifié.
//    public void saveSampledToJson(File outputFile, double step) throws IOException {
//        double[][] sampledData = sample(step);
//
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNodeFactory nodeFactory = mapper.getNodeFactory();
//        ObjectNode rootNode = nodeFactory.objectNode();
//
//        // Création de l'objet "Enveloppes"
//        ObjectNode enveloppesNode = nodeFactory.objectNode();
//        enveloppesNode.put("Dt_ms", dtMs);
//
//        // Création de l'objet "Capteurs"
//        ArrayNode capteursArrayNode = nodeFactory.arrayNode();
//
//        // Ajout des données du premier capteur
//        ObjectNode capteur1Node = nodeFactory.objectNode();
//        ArrayNode xNode1 = nodeFactory.arrayNode();
//        ArrayNode yNode1 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode1.add(sampledData[0][i]);
//            yNode1.add(sampledData[1][i]);
//        }
//
//        capteur1Node.set("X", xNode1);
//        capteur1Node.set("Y", yNode1);
//
//        capteursArrayNode.add(capteur1Node);
//
//        // Ajout des données du deuxième capteur
//        ObjectNode capteur2Node = nodeFactory.objectNode();
//        ArrayNode xNode2 = nodeFactory.arrayNode();
//        ArrayNode yNode2 = nodeFactory.arrayNode();
//
//        for (int i = sampledData[0].length / 2; i < sampledData[0].length; i++) {
//            xNode2.add(sampledData[0][i]);
//            yNode2.add(sampledData[1][i]);
//        }
//
//        capteur2Node.set("X", xNode2);
//        capteur2Node.set("Y", yNode2);
//
//        capteursArrayNode.add(capteur2Node);
//
//
//        // Ajout des données du troisieme capteur
//        ObjectNode capteur3Node = nodeFactory.objectNode();
//        ArrayNode xNode3 = nodeFactory.arrayNode();
//        ArrayNode yNode3 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode3.add(sampledData[0][i]);
//            yNode3.add(sampledData[1][i]);
//        }
//
//        capteur3Node.set("X", xNode3);
//        capteur3Node.set("Y", yNode3);
//
//        capteursArrayNode.add(capteur3Node);
//
//
//        // Ajout des données du 4 capteur
//        ObjectNode capteur4Node = nodeFactory.objectNode();
//        ArrayNode xNode4 = nodeFactory.arrayNode();
//        ArrayNode yNode4 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode4.add(sampledData[0][i]);
//            yNode4.add(sampledData[1][i]);
//        }
//
//        capteur4Node.set("X", xNode4);
//        capteur4Node.set("Y", yNode4);
//
//        capteursArrayNode.add(capteur4Node);
//
//
//        // Ajout des données du 5 capteur
//        ObjectNode capteur5Node = nodeFactory.objectNode();
//        ArrayNode xNode5 = nodeFactory.arrayNode();
//        ArrayNode yNode5 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode5.add(sampledData[0][i]);
//            yNode5.add(sampledData[1][i]);
//        }
//
//        capteur5Node.set("X", xNode5);
//        capteur5Node.set("Y", yNode5);
//
//        capteursArrayNode.add(capteur5Node);
//
//
//        // Ajout des données du 6 capteur
//        ObjectNode capteur6Node = nodeFactory.objectNode();
//        ArrayNode xNode6 = nodeFactory.arrayNode();
//        ArrayNode yNode6 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode6.add(sampledData[0][i]);
//            yNode6.add(sampledData[1][i]);
//        }
//
//        capteur6Node.set("X", xNode6);
//        capteur6Node.set("Y", yNode6);
//
//        capteursArrayNode.add(capteur6Node);
//
//
//        // Ajout des données du 7 capteur
//        ObjectNode capteur7Node = nodeFactory.objectNode();
//        ArrayNode xNode7 = nodeFactory.arrayNode();
//        ArrayNode yNode7 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode7.add(sampledData[0][i]);
//            yNode7.add(sampledData[1][i]);
//        }
//
//        capteur7Node.set("X", xNode7);
//        capteur7Node.set("Y", yNode7);
//
//        capteursArrayNode.add(capteur7Node);
//
//
//        // Ajout des données du 8 capteur
//        ObjectNode capteur8Node = nodeFactory.objectNode();
//        ArrayNode xNode8 = nodeFactory.arrayNode();
//        ArrayNode yNode8 = nodeFactory.arrayNode();
//
//        for (int i = 0; i < sampledData[0].length / 2; i++) {
//            xNode8.add(sampledData[0][i]);
//            yNode8.add(sampledData[1][i]);
//        }
//
//        capteur8Node.set("X", xNode8);
//        capteur8Node.set("Y", yNode8);
//
//        capteursArrayNode.add(capteur8Node);
//
//
//
//        enveloppesNode.set("Capteurs", capteursArrayNode);
//        rootNode.set("Enveloppes", enveloppesNode);
//
//        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, rootNode);
//    }
//
//
//
//    public static void generateGraph(File jsonFile) throws IOException {
//
//        // Charger le fichier JSON
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode rootNode = mapper.readTree(jsonFile);
//        JsonNode enveloppeNode = rootNode.get("Enveloppes");
//
//        // Récupérer les données de Capteurs[0]
//        JsonNode capteursNode = enveloppeNode.get("Capteurs").get(0);
//        JsonNode xNode = capteursNode.get("X");
//        JsonNode yNode = capteursNode.get("Y");
//
//        // Créer une série de données pour le graphe
//        XYSeries series = new XYSeries("Données de capteur");
//        double intervalle = 0.2; // intervalle en millisecondes entre chaque vague
//        double lastX = 0.0; // valeur X du dernier point ajouté
//        for (int i = 0; i < xNode.size(); i++) {
//            double x = xNode.get(i).asDouble();
//            double y = yNode.get(i).asDouble();
//            if (y <= 0.4 && x <= 2100000) { // ne garder que les points dont la valeur en Y est inferieur ou égale à 0.4 et x inferieur a 2100000
//                series.add(x, y, false);
//
//                // Ajouter un point supplémentaire si l'intervalle entre le dernier point ajouté et le point actuel est plus grand que l'intervalle souhaité
//                if (x - lastX > intervalle) {
//                    int nbVagues = (int) ((x - lastX) / intervalle); // nombre de vagues à ajouter
//                    for (int j = 1; j < nbVagues; j++) {
//                        double newX = lastX + j * intervalle; // calculer la valeur X du point à ajouter
//                        series.add(newX, y, false); // ajouter le point
//                    }
//                }
//
//                lastX = x; // mettre à jour la valeur X du dernier point ajouté
//            }
//        }
//
//
//
//
//        // Ajouter la série de données à une collection
//        XYSeriesCollection dataset = new XYSeriesCollection();
//        dataset.addSeries(series);
//
//        // Créer le graphe
//        JFreeChart chart = ChartFactory.createXYLineChart(
//                "Données de capteur", // titre
//                "Temps (ms)", // étiquette axe des abscisses
//                "Valeur", // étiquette axe des ordonnées
//                dataset, // données
//                PlotOrientation.VERTICAL, // orientation du graphe
//                true, // afficher la légende
//                false, // pas de tooltips
//                false // pas de URLs
//        );
//
//        // Personnaliser le graphe
//        XYPlot plot = chart.getXYPlot();
//        plot.setBackgroundPaint(Color.white);
//        plot.setRangeGridlinePaint(Color.black);
//        plot.setDomainGridlinePaint(Color.black);
//
//
//        // Personnaliser l'axe des abscisses
//        NumberAxis xAxis = new NumberAxis("Temps (ms)");
//        xAxis.setLowerBound(0); // Fixer la valeur minimale à 0
//        xAxis.setRange(0, 2500000);
//        xAxis.setTickUnit(new NumberTickUnit(275000));
//        xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits(Locale.FRANCE));
//        plot.setDomainAxis(xAxis);
//        xAxis.setTickMarkInsideLength(2f); // Réduire l'espace entre les points
//        xAxis.setTickMarkOutsideLength(0f);
//
//
//
//
//        // Personnaliser l'axe des ordonnées
//        NumberAxis yAxis = new NumberAxis("Valeur");
////        yAxis.setFixedDimension(50);
//
//
//        yAxis.setRange(0, 0.4);
//        yAxis.setTickUnit(new NumberTickUnit(0.1));
//        plot.setRangeAxis(yAxis);
//
//
//
//// Générer l'image du graphe
//        File outputFile = new File("C:\\Users\\Ilham Barache\\Documents\\output\\image.png");
//        ChartUtilities.saveChartAsPNG(outputFile, chart, 3000, 600);
//
//
//    }
//}
