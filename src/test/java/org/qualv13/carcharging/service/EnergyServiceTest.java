package org.qualv13.carcharging.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock
    private CarbonIntensityClient client;

    @InjectMocks
    private EnergyService energyService;



    @Test
    void shouldCalculateDailyAverageCorrectly() {
        // GIVEN: 2 slots from same day.
        // Slot 1: 50% wind
        // Slot 2: 100% wind
        // Expected result: 75% clean energy
        List<GenerationData> mockData = List.of(
                createData("2023-11-25T10:00Z", "2023-11-25T10:30Z", "wind", 50.0),
                createData("2023-11-25T14:00Z", "2023-11-25T14:30Z", "wind", 100.0)
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();

        // THEN
        assertEquals(1, result.size()); // 1 day only
        assertEquals("2023-11-25", result.get(0).getDate());
        assertEquals(75.0, result.get(0).getCleanEnergyPercent(), 0.01);
    }

    private GenerationData createData(String from, String to, String fuelName, double perc) {
        FuelMix fuelMix = new FuelMix();
        return new GenerationData(from, to, List.of(new FuelMix(fuelName, perc)));
    }
}