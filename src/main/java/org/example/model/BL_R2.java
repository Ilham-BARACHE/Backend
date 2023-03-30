package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.List;

@Data
@Embeddable
public class BL_R2 {

    @ElementCollection
    @JsonProperty("X")
    private List<Double> x;

    @ElementCollection
    @JsonProperty("Y")
    private List<Double> y;

    @ElementCollection
    @JsonProperty("Z")
    private List<Double> z;


    @ElementCollection
    @JsonProperty("X_Fond")
    private List<String> xFond;

    @ElementCollection
    @JsonProperty("Y_Fond")
    private List<String> yFond;


    @ElementCollection
    @JsonProperty("Z_Fond")
    private List<String> zFond;

    public  BL_R2(){}

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getY() {
        return y;
    }

    public void setY(List<Double> y) {
        this.y = y;
    }

    public List<Double> getZ() {
        return z;
    }

    public void setZ(List<Double> z) {
        this.z = z;
    }

    public List<String> getxFond() {
        return xFond;
    }

    public void setxFond(List<String> xFond) {
        this.xFond = xFond;
    }

    public List<String> getyFond() {
        return yFond;
    }

    public void setyFond(List<String> yFond) {
        this.yFond = yFond;
    }

    public List<String> getzFond() {
        return zFond;
    }

    public void setzFond(List<String> zFond) {
        this.zFond = zFond;
    }


    public BL_R2(List<Double> x, List<Double> y, List<Double> z, List<String> xFond, List<String> yFond, List<String> zFond) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xFond = xFond;
        this.yFond = yFond;
        this.zFond = zFond;
    }
}
