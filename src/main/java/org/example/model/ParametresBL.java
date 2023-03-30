package org.example.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.PostLoad;
import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class ParametresBL {


    @ElementCollection(targetClass = Double.class)
    private List<List<Double>> parametresBl1 = new ArrayList<>(33);




   public ParametresBL(){}


}
