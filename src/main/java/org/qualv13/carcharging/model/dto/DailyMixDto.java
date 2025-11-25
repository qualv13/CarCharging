package org.qualv13.carcharging.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyMixDto {
    private String date;
    private Double cleanEnergyPercent;
    private Map<String, Double> dailyMix;
}
