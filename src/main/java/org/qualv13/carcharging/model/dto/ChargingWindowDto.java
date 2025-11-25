package org.qualv13.carcharging.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingWindowDto {
    private String StartTime;
    private double CleanEnergyPercent;
}
