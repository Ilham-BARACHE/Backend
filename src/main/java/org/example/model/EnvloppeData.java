package org.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

public class EnvloppeData {

    private List<Double> x;
    private List<Double> y;
    private double dtMs;
    private List<Double> x1;
    private List<Double> y1;

    private List<Double> x2;
    private List<Double> y2;

    private List<Double> x3;
    private List<Double> y3;
    private List<Double> x4;
    private List<Double> y4;
    private List<Double> x5;
    private List<Double> y5;
    private List<Double> x6;
    private List<Double> y6;
    private List<Double> x7;
    private List<Double> y7;

    public List<Double> getX() {
        return x;
    }

    public List<Double> getY() {
        return y;
    }


    public double getDtMs() {
        return dtMs;
    }

    public void setDtMs(double dtMs) {
        this.dtMs = dtMs;
    }

    private double[][] bornes;
    private double TempsMin;
    private double TempsMax;

    public EnvloppeData() {
        x = new ArrayList<>();
        y = new ArrayList<>();
        x1 = new ArrayList<>();
        y1 = new ArrayList<>();

        x2 = new ArrayList<>();
        y2 = new ArrayList<>();
        x3 = new ArrayList<>();
        y3 = new ArrayList<>();

        x4 = new ArrayList<>();
        y4 = new ArrayList<>();
        x5 = new ArrayList<>();
        y5 = new ArrayList<>();

        x6 = new ArrayList<>();
        y6 = new ArrayList<>();
        x7 = new ArrayList<>();
        y7 = new ArrayList<>();
        dtMs = 0.0;

    }

    public void loadFromJson(File jsonFile) throws IOException {


        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
        JsonNode enveloppeNode = rootNode.has("Enveloppes") ? rootNode.get("Enveloppes") : null;



        if (enveloppeNode != null && !enveloppeNode.isEmpty()) {
            for (int i = 0; i < enveloppeNode.size(); i++) {
            JsonNode capteursNode = enveloppeNode.get("Capteurs").get(i);
            JsonNode xNode = capteursNode.get("X");
            JsonNode yNode = capteursNode.get("Y");
            for (int j = 0; j < xNode.size(); j++) {
                x.add(xNode.get(j).asDouble());
                y.add(yNode.get(j).asDouble());


            }
        }
}else {
            return;
        }
    }


    //calcule les bornes inférieures et supérieures des données enregistrées en x et y
    public void CalculerBornes() {



        double borneInfX = Collections.min(x);

        double borneSupX = Collections.max(x);
        String borneSupXString = String.valueOf((long) borneSupX);
        double borneSupXValue = Double.parseDouble(borneSupXString);




        double borneInfY = Collections.min(y);
        double borneSupY = Collections.max(y);

        this.bornes = new double[][] { {borneInfX, borneInfY}, {borneSupXValue, borneSupY} };
        this.TempsMin = bornes[0][0];
        this.TempsMax = bornes[1][0];
    }

    //permet de garder uniquement une partie des données qui se situent entre deux bornes de temps spécifiées
    public void GarderSegment(double tempsMin, double tempsMax) {
        List<Double> newX = new ArrayList<>();
        List<Double> newY = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i) >= tempsMin && x.get(i) <= tempsMax ) {
                newX.add(x.get(i));
                newY.add(y.get(i));
            }
        }
        x = newX;
        y = newY;
        // ajuste les bornes pour contenir toutes les données
        if (TempsMin < tempsMin) {
            TempsMin = tempsMin;
        }
        if (TempsMax > tempsMax) {
            TempsMax = tempsMax;
        }

    }

    //méthode principale qui échantillonne les données. Elle calcule d'abord les bornes de temps et les ajuste pour contenir toutes les données. Ensuite, elle trouve les indices du début et de la fin de chaque "segment" de données (où la valeur de y est inférieure à 0,2) et calcule les bornes correspondantes en x. Enfin, elle échantillonne les données en fonction d'un pas donné (step) pour obtenir un nombre fixe de points échantillonnés
    public double[][] sample(double step) {


        List<Double> lsttmpmin = new ArrayList<Double>();
        List<Double> lsttmpmax = new ArrayList<Double>();
        EnvloppeData enveloppeData = this;


        enveloppeData.CalculerBornes();

        lsttmpmin.add(enveloppeData.TempsMin);
        lsttmpmax.add(enveloppeData.TempsMax);

        double tempsMin = (Math.floor(Collections.min(lsttmpmin) / 10000) - 1) * 10000d;
        double tempsMax = (Math.ceil(Collections.max(lsttmpmax) / 10000) + 1) * 10000d;


        enveloppeData.GarderSegment(tempsMin, tempsMax);

        // Pour chaque capteur, trouver la première et la dernière valeur en dessous de 0.2 dans le tableau Y.
        List<Integer> firstIndices = new ArrayList<Integer>();
        List<Integer> lastIndices = new ArrayList<Integer>();
        double minY = 0.2;
        for (int i = 0; i < y.size(); i++) {
            if (y.get(i) < minY) {
                if (firstIndices.isEmpty()) {
                    firstIndices.add(i);
                }
                lastIndices.clear();
                lastIndices.add(i);
            } else {
                if (!lastIndices.isEmpty()) {
                    firstIndices.add(lastIndices.get(0));
                    lastIndices.clear();
                }
            }
        }

        // Déterminer la valeur minimale de toutes les premières valeurs (XP) et la valeur maximale de toutes les dernières valeurs (XD).
        double xp = Double.MAX_VALUE;
        double xd = Double.MIN_VALUE;
        for (int i = 0; i < lastIndices.size(); i++) {
            int lastIndex = lastIndices.get(i);
            int firstIndex = firstIndices.get(i);
            if (i > 0) {
                firstIndex = lastIndices.get(i - 1) ;
            }
            double xFirst = x.get(firstIndex);
            double xLast = x.get(lastIndex);
            xp = Math.min(xp, xFirst);
            xd = Math.max(xd, xLast);
        }





        // Calculer le nombre de valeurs entre XP et XD et diviser ce nombre par la largeur maximale des images souhaitée (par exemple, 6000).
        long numSamples = (long) Math.ceil( (xd - xp) / step);


        int maxSamples = 6000;
        if (numSamples <= maxSamples) {
            return sampleAllData();
        } else {
            return sampleDataWithFixedWidth(xp, xd, maxSamples);
        }

    }

    //échantillonne toutes les données à intervalles réguliers
    private double[][] sampleAllData() {
        double[][] sampledData = new double[2][x.size()];
        for (int i = 0; i < x.size(); i++) {
            sampledData[0][i] = x.get(i);
            sampledData[1][i] = y.get(i);
        }
        return sampledData;
    }

    //échantillonne les données en conservant une largeur fixe pour l'ensemble des données échantillonnées
    private double[][] sampleDataWithFixedWidth(double xp, double xd, int numSamples) {
        double step = (xd - xp) / (numSamples - 1);
        double[][] sampledData = new double[2][numSamples];
        for (int i = 0; i < numSamples; i++) {
            double xSampled = xp + i * step;
            double ySampled = interpolateY(xSampled);
            sampledData[0][i] = xSampled;
            sampledData[1][i] = ySampled;
        }
        return sampledData;
    }

//interpole une valeur de y en fonction d'une valeur donnée de x en utilisant une approximation linéaire entre les deux points les plus proches.
    private double interpolateY(double xSampled) {
        int i = 0;
        while (i < x.size() - 1 && x.get(i) < xSampled) {
            i++;
        }
        if (i == 0) {
            return y.get(0);
        } else if (i == x.size()) {
            return y.get(x.size() - 1);
        } else {
            double x1 = x.get(i - 1);
            double x2 = x.get(i);
            double y1 = y.get(i - 1);
            double y2 = y.get(i);
            double slope = (y2 - y1) / (x2 - x1);
            double ySampled = y1 + slope * (xSampled - x1);
            return ySampled;
        }
    }
    //échantillonne les données et les sauvegarde au format JSON dans un fichier spécifié.
    public void saveSampledToJson(File outputFile, double step) throws IOException {
        double[][] sampledData = sample(step);
        System.out.println("Je suis la " + Arrays.deepToString(sampledData));

        ObjectMapper mapper = new ObjectMapper();
        JsonNodeFactory nodeFactory = mapper.getNodeFactory();
        ObjectNode rootNode = nodeFactory.objectNode();

// Création de l'objet "Enveloppes"
        ObjectNode enveloppesNode = nodeFactory.objectNode();
        enveloppesNode.put("Dt_ms", dtMs);

// Création de l'objet "Capteurs"
        ArrayNode capteursArrayNode = nodeFactory.arrayNode();

// Boucle pour créer les noeuds de chaque capteur
        for (int capteurIndex = 0; capteurIndex < sampledData.length; capteurIndex++) {
            ObjectNode capteurNode = nodeFactory.objectNode();
            ArrayNode xNode = nodeFactory.arrayNode();
            ArrayNode yNode = nodeFactory.arrayNode();

            for (int i = 0; i < sampledData[capteurIndex].length; i += 2) {
                xNode.add(sampledData[capteurIndex][i]);
                yNode.add(sampledData[capteurIndex][i+1]);
            }


            capteurNode.set("X", xNode);
            capteurNode.set("Y", yNode);

            capteursArrayNode.add(capteurNode);
        }



// Ajout du tableau de capteurs à l'objet principal
        rootNode.set("Enveloppes", enveloppesNode);
        rootNode.set("Capteurs", capteursArrayNode);



        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, rootNode);
    }



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
//            if (y <= 0.4) { // ne garder que les points dont la valeur en Y est inferieur ou égale à 0.4
//                if (x <= 3000000) { // si la valeur de x est inférieure ou égale à 3000000, ajouter le point
//                    series.add(x, y, false);
//                } else { // sinon, ajouter un point supplémentaire à 3000000 avec la même valeur de y
//                    series.add(3000000, y, false);
//                }
//
//                // Ajouter un point supplémentaire si l'intervalle entre le dernier point ajouté et le point actuel est plus grand que l'intervalle souhaité
//                if (x - lastX > intervalle) {
//                    int nbVagues = (int) ((x - lastX) / intervalle); // nombre de vagues à ajouter
//                    for (int j = 1; j < nbVagues; j++) {
//                        double newX = lastX + j * intervalle; // calculer la valeur X du point à ajouter
//                        if (newX <= 3000000) { // si la valeur de x est inférieure ou égale à 3000000, ajouter le point
//                            series.add(newX, y, false); // ajouter le point
//                        } else { // sinon, ajouter un point supplémentaire à 3000000 avec la même valeur de y
//                            series.add(3000000, y, false);
//                        }
//                    }
//                }
//// Ajouter un point fictif si la différence entre la valeur x du point actuel et la valeur x du dernier point ajouté est suffisamment grande
//                if (x - lastX > 90000) {
//                    double newX = x - 9; // calculer la valeur X du point fictif
//                    series.add(newX, 0, false); // ajouter le point fictif avec une valeur y nulle
//                }
//                lastX = x; // mettre à jour la valeur X du dernier point ajouté
//            }
//
//
//
//
//
//
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
//
//        // Personnaliser l'axe des abscisses
//        NumberAxis xAxis = new NumberAxis("Temps (ms)");
//        xAxis.setLowerBound(0); // Fixer la valeur minimale à 0
//        xAxis.setRange(0, 3000000);
//        xAxis.setTickUnit(new NumberTickUnit(400000));
//        xAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits(Locale.FRANCE));
//        plot.setDomainAxis(xAxis);
//
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
//        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
//        renderer.setBaseStroke(new BasicStroke(0.1f));
//
//
//
//// Générer l'image du graphe
//        File outputFile = new File("C:\\Users\\Ilham Barache\\Documents\\output\\image.png");
//        ChartUtilities.saveChartAsPNG(outputFile, chart, 4000, 180);
//
//
//
//    }
}
