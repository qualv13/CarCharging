package org.qualv13.carcharging.model.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationData {

    private String from;
    private String to;
    private List<FuelMix> generationmix;
}