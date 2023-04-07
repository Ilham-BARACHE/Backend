package org.example.model;

import java.util.List;

public class Capteur {

    private List<Double> xList;
    private List<Double> yList;

    public Capteur(List<Double> xList, List<Double> yList) {
        this.xList = xList;
        this.yList = yList;
    }

    public List<Double> getXList() {
        return xList;
    }

    public List<Double> getYList() {
        return yList;
    }


}
