package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class Temps_ms {
    @JsonProperty("t1")
    @ElementCollection(targetClass = Double.class)
    private List<List<Double>> t1 = new ArrayList<>(33);

    @JsonProperty("t2")
    @ElementCollection(targetClass = Double.class)
    private List<List<Double>> t2 = new ArrayList<>(33);

    @JsonProperty("t3")
    @ElementCollection(targetClass = Double.class)
    private List<List<Double>> t3 = new ArrayList<>(33);


    public Temps_ms(List<List<Double>> t1, List<List<Double>> t2, List<List<Double>> t3) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    public Temps_ms() {

    }





}
