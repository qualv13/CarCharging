package org.qualv13.carcharging.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DailyMixDto {
    private String date;
    private Double cleanEnergyPercent;
    private Map<String, Double> dailyMix;
}
