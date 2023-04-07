package org.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class Capteur {

    private List<Double> x;
    private List<Double> y;

    public Capteur(List<Double> x, List<Double> y) {
        this.x = x;
        this.y = y;
    }

    public List<Double> getX() {
        return x;
    }

    public List<Double> getY() {
        return y;
    }



}
