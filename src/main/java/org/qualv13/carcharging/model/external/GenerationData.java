package org.qualv13.carcharging.model.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data // Generuje Gettery, Settery, toString, equals, hashCode
@AllArgsConstructor // Generuje konstruktor ze wszystkimi polami (przydatne w testach)
@NoArgsConstructor // Generuje pusty konstruktor (WYMAGANE przez Jacksona do JSON)
public class GenerationData {

    private String from;
    private String to;
    private List<FuelMix> generationmix;
}