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

    List<Double> x = new ArrayList<>();
    List<Double> y = new ArrayList<>();
    public double dtMs;





    private double[][] bornes;
    private double TempsMin;
    private double TempsMax;

    public EnvloppeData() {

        x = new ArrayList<>();
        y = new ArrayList<>();

        dtMs = 0.0;

    }

    public void loadFromJson(File jsonFile, int j) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);

        x.clear();
        y.clear();

        JsonNode enveloppesNode = rootNode.has("Enveloppes") ? rootNode.get("Enveloppes") : null;

        if (enveloppesNode != null) {
            if (enveloppesNode.has("dt_ms") && !enveloppesNode.get("dt_ms").isNull()) {
                dtMs = enveloppesNode.get("dt_ms").asDouble();
            }
            System.out.println("voila dt_ms "+dtMs);

            JsonNode capteursNode = enveloppesNode.has("Capteurs") ? enveloppesNode.get("Capteurs").get(j - 1) : null;

            if (capteursNode != null) {
                JsonNode xNode = capteursNode.get("X");
                JsonNode yNode = capteursNode.get("Y");
                for (int i = 0; i < xNode.size(); i++) {
                    x.add(xNode.get(i).asDouble());
                    y.add(yNode.get(i).asDouble());
                }
            }
        }
    }





    //calcule les bornes inférieures et supérieures des données enregistrées en x et y
    public void CalculerBornes() {

        if (!x.isEmpty() && !y.isEmpty()) {

            double borneInfX = Collections.min(x);

            double borneSupX = Collections.max(x);
            String borneSupXString = String.valueOf((long) borneSupX);
            double borneSupXValue = Double.parseDouble(borneSupXString);


            double borneInfY = Collections.min(y);
            double borneSupY = Collections.max(y);

            this.bornes = new double[][]{{borneInfX, borneInfY}, {borneSupXValue, borneSupY}};
            this.TempsMin = bornes[0][0];
            this.TempsMax = bornes[1][0];
        }
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
        List<Integer> highIndices = new ArrayList<Integer>();
        List<Integer> firstIndices = new ArrayList<Integer>();
        List<Integer> lastIndices = new ArrayList<Integer>();
        double minY = 0.2;

        for (int i = 0; i < y.size(); i++) {
            if (y.get(i) >= minY && x.get(i) >= 0) { // vérifie si la valeur de Y est supérieure à 0,2
                highIndices.add(i);
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
                lastIndices.add(i); // Ajout de l'index actuel
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
            double yFirst = y.get(firstIndex);
            double yLast = y.get(lastIndex);
            if (yFirst >= minY || yLast >= minY) { // vérifier si la première ou la dernière valeur est supérieure ou égale à la valeur minimale
                xp = Math.min(xp, xFirst);
                xd = Math.max(xd, xLast);
            }
        }


// Échantillonnez les données correspondant aux indices des valeurs Y supérieures à 0,2
        List<Double> ySample = new ArrayList<Double>();
        List<Double> xSample = new ArrayList<Double>();
        for (int i : highIndices) {
            ySample.add(y.get(i));

            xSample.add(x.get(i));
        }


        // Calculer le nombre de valeurs entre XP et XD et diviser ce nombre par la largeur maximale des images souhaitée (par exemple, 6000).
        long numSamples = (long) Math.ceil( (xd - xp) / step);


        int maxSamples = 10000;
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
            sampledData[0][i] = Math.round(xSampled);
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

        ObjectMapper mapper = new ObjectMapper();
        JsonNodeFactory nodeFactory = mapper.getNodeFactory();
        ObjectNode rootNode = nodeFactory.objectNode();

        // Effacer les nœuds existants dans l'objet rootNode
        rootNode.removeAll();
        // Création de l'objet "Enveloppes"
        ObjectNode enveloppesNode = nodeFactory.objectNode();



        // Création de l'objet "Capteurs"
        ArrayNode capteursArrayNode = enveloppesNode.arrayNode();

        // Ajout des données du premier capteur
        ObjectNode capteurNode = nodeFactory.objectNode();
        ArrayNode xNode = nodeFactory.arrayNode();
        ArrayNode yNode = nodeFactory.arrayNode();




        for (int i = 0; i < sampledData[0].length; i++) {
            if ((sampledData[0][i]) >= 0) {
                Double xValue = sampledData[0][i] * dtMs;

                xNode.add(xValue);

                Double yValue = sampledData[1][i];

                yNode.add(yValue);




            }
        }


        System.out.println(xNode.get(1));
        capteurNode.set("X", xNode);
        capteurNode.set("Y", yNode);

        capteursArrayNode.add(capteurNode);



// Ajout du nouveau contenu à l'objet rootNode


        rootNode.set("Capteurs", capteursArrayNode);

// Écrire le fichier JSON
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, rootNode);

    }



    public static void generateGraph(File jsonFile , int j , double k ) throws IOException {

        // Charger le fichier JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);


        // Récupérer les données de Capteurs[0]
        JsonNode capteursNode = rootNode.get("Capteurs").get(0);

        JsonNode xNode = capteursNode.get("X");


        JsonNode yNode = capteursNode.get("Y");

        // Créer une série de données pour le graphe
        XYSeries series = new XYSeries("Enveloppe (Signaux)");

        double lastX = Double.NEGATIVE_INFINITY; // initialiser la valeur X du dernier point ajouté avec une valeur très petite
        double firstX = 0.0 ;
        double firstY = 0.0;
        boolean foundFirstChange = false;
        for (int i = 0; i < xNode.size(); i++) {
            double x = xNode.get(i).asDouble();
            double y = yNode.get(i).asDouble();

            if (!foundFirstChange) {
                firstX = x;
                firstY = y;
                foundFirstChange = true;
            }





            if (y <= 0.4 ) {
                series.add(x, y); // ajouter le point à la série de données



                lastX = x; // mettre à jour la valeur du dernier X


            }


        }

        System.out.println("Premier couple (x, y) du premier changement : (" + firstX + ", " + firstY + ")");







        // Ajouter la série de données à une collection
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // Créer le graphe
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Données de capteur", // titre
                "Temps (s)", // étiquette axe des abscisses
                "Tension (v)", // étiquette axe des ordonnées
                dataset, // données
                PlotOrientation.VERTICAL, // orientation du graphe
                true, // afficher la légende
                false, // pas de tooltips
                false // pas de URLs
        );

        // Personnaliser le graphe
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.black);



        // Personnaliser l'axe des abscisses
        NumberAxis xAxis = new NumberAxis("Temps (s)");
        xAxis.setLowerBound(0); // Fixer la valeur minimale à 0
System.out.println("premier point "+firstX+" la fi  est "+(firstX+(k/1000)));
Double f = firstX+(k/1000);
System.out.println(f);
        xAxis.setRange(firstX, f * 1000);

        plot.setDomainAxis(xAxis);





        // Personnaliser l'axe des ordonnées
        NumberAxis yAxis = new NumberAxis("Tension (v)");


        yAxis.setRange(0, 0.4);
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        plot.setRangeAxis(yAxis);






// Générer l'image du graphe
        String jsonFileName = jsonFile.getName().replace(".json", "");
        File outputFile = new File(jsonFile.getParentFile(), jsonFileName+"_Capteur"+ j + ".png");

        ChartUtilities.saveChartAsPNG(outputFile, chart, 4000, 180);



    }
}
