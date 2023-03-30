package org.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnvloppeData {

    private List<Double> x;
    private List<Double> y;
    private double dtMs;

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
        dtMs = 0.0;
        TempsMin = Double.POSITIVE_INFINITY;
        TempsMax = Double.NEGATIVE_INFINITY;
    }

    public void loadFromJson(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);
        JsonNode enveloppeNode = rootNode.get("Enveloppes");
        dtMs = enveloppeNode.get("dt_ms").asDouble();
        JsonNode capteursNode = enveloppeNode.get("Capteurs");
        for (JsonNode node : capteursNode) {
            JsonNode xNode = node.get("X");
            JsonNode yNode = node.get("Y");
            for (int i = 0; i < xNode.size(); i++) {
                x.add(xNode.get(i).asDouble());
                y.add(yNode.get(i).asDouble());
            }
        }
    }


    //calcule les bornes inférieures et supérieures des données enregistrées en x et y
    public void CalculerBornes() {


        double borneInfX = Collections.min(x);
        double borneSupX = Collections.max(x);
        double borneInfY = Collections.min(y);
        double borneSupY = Collections.max(y);
        this.bornes = new double[][] { {borneInfX, borneInfY}, {borneSupX, borneSupY} };
    }

    //permet de garder uniquement une partie des données qui se situent entre deux bornes de temps spécifiées
    public void GarderSegment(double tempsMin, double tempsMax) {
        List<Double> newX = new ArrayList<>();
        List<Double> newY = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i) >= tempsMin && x.get(i) <= tempsMax) {
                newX.add(x.get(i));
                newY.add(y.get(i));
            }
        }
        x = newX;
        y = newY;


    }

    //méthode principale qui échantillonne les données. Elle calcule d'abord les bornes de temps et les ajuste pour contenir toutes les données. Ensuite, elle trouve les indices du début et de la fin de chaque "segment" de données (où la valeur de y est inférieure à 0,2) et calcule les bornes correspondantes en x. Enfin, elle échantillonne les données en fonction d'un pas donné (step) pour obtenir un nombre fixe de points échantillonnés
    public double[][] sample(double step) {

        List<Double> lsttmpmin = new ArrayList<Double>();
        List<Double> lsttmpmax = new ArrayList<Double>();
        EnvloppeData enveloppeData = this;

        enveloppeData.CalculerBornes();
        lsttmpmin.add(enveloppeData.TempsMin);
        lsttmpmax.add(enveloppeData.TempsMax);

        double tempsMin = (Math.floor(Collections.min(lsttmpmin) / 10000) - 1) * 10000;
        double tempsMax = (Math.ceil(Collections.max(lsttmpmax) / 10000) + 1) * 10000;

        System.out.println(tempsMin);
        System.out.println(tempsMax);
        enveloppeData.GarderSegment(tempsMin, tempsMax);

        // Trouver les bornes de temps (segmentBounds) pour chaque segment de données.
        List<Double[]> segmentBounds = new ArrayList<Double[]>();
        double[] startBound = new double[] {0.0, 0.0}; // initialiser startBound
        double minY = 0.2;
        for (int i = 0; i < y.size(); i++) {
            if (y.get(i) < minY) {
                if (startBound[1] == 0.0) { // si startBound n'a pas encore été initialisé, l'initialiser avec i-1
                    startBound = new double[] {x.get(i-1), y.get(i-1)};
                }
            } else {
                if (startBound[1] != 0.0) { // si startBound a été initialisé et y >= minY, initialiser endBound avec i
                    double[] endBound = new double[] {x.get(i), y.get(i)};
                    segmentBounds.add(new Double[] {startBound[0], startBound[1], endBound[0], endBound[1]});
                    startBound = new double[] {0.0, 0.0}; // réinitialiser startBound
                }
            }
        }
        if (startBound[1] != 0.0) { // si startBound n'a pas été utilisé, l'utiliser avec la dernière valeur de x et y
            double[] endBound = new double[] {x.get(x.size()-1), y.get(y.size()-1)};
            segmentBounds.add(new Double[] {startBound[0], startBound[1], endBound[0], endBound[1]});
        }

        // Trouver la valeur minimale de x pour tous les segments de données et la valeur maximale de x pour tous les segments de données.
        double xp = Double.MAX_VALUE;
        double xd = Double.MIN_VALUE;
        for (Double[] bounds : segmentBounds) {
            if (bounds[0] < xp) {
                xp = bounds[0];
            }
            if (bounds[2] > xd) {
                xd = bounds[2];
            }
        }

        // Calculer le nombre de valeurs entre XP et XD et diviser ce nombre par la largeur maximale des images souhaitée (par exemple, 6000).
        int numSamples = (int) Math.ceil((xd - xp) / step);
        System.out.println(String.format("xp est :"+ xp +"xd est :"+ xd));
        System.out.println("voila la valeur de numSamples"+numSamples);

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

        ObjectMapper mapper = new ObjectMapper();
        JsonNodeFactory nodeFactory = mapper.getNodeFactory();
        ObjectNode rootNode = nodeFactory.objectNode();

        // Création de l'objet "Enveloppes"
        ObjectNode enveloppesNode = nodeFactory.objectNode();
        enveloppesNode.put("Dt_ms", dtMs);

        // Création de l'objet "Capteurs"
        ObjectNode capteursNode = nodeFactory.objectNode();
        ArrayNode xNode = nodeFactory.arrayNode();
        ArrayNode yNode = nodeFactory.arrayNode();

        for (int i = 0; i < sampledData[0].length; i++) {
            xNode.add(sampledData[0][i]);
            yNode.add(sampledData[1][i]);
        }

        capteursNode.set("X", xNode);
        capteursNode.set("Y", yNode);

        enveloppesNode.set("capteurs", capteursNode);
        rootNode.set("enveloppes", enveloppesNode);

        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, rootNode);
    }





}
