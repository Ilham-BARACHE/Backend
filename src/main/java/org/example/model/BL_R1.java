package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.List;

@Data
@Embeddable
public class BL_R1 {

    @ElementCollection
    @JsonProperty("X")
    private List<Double> xl;

    @ElementCollection
    @JsonProperty("Y")
    private List<Double> yl;

    @ElementCollection
    @JsonProperty("Z")
    private List<Double> zl;


    @ElementCollection
    @JsonProperty("X_Fond")
    private List<String> xFondl;

    @ElementCollection
    @JsonProperty("Y_Fond")
    private List<String> yFondl;


    @ElementCollection
    @JsonProperty("Z_Fond")
    private List<String> zFondl;

    public  BL_R1(){}

    public List<Double> getXl() {
        return xl;
    }

    public void setXl(List<Double> xl) {
        this.xl = xl;
    }

    public List<Double> getYl() {
        return yl;
    }

    public void setYl(List<Double> yl) {
        this.yl = yl;
    }

    public List<Double> getZl() {
        return zl;
    }

    public void setZl(List<Double> zl) {
        this.zl = zl;
    }

    public List<String> getxFondl() {
        return xFondl;
    }

    public void setxFondl(List<String> xFondl) {
        this.xFondl = xFondl;
    }

    public List<String> getyFondl() {
        return yFondl;
    }

    public void setyFondl(List<String> yFondl) {
        this.yFondl = yFondl;
    }

    public List<String> getzFondl() {
        return zFondl;
    }

    public void setzFondl(List<String> zFondl) {
        this.zFondl = zFondl;
    }
}
