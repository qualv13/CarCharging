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
    void shouldFindBestChargingWindow_WhenDataIsPerfect() {
        // GIVEN: 4 slots (2h).
        // Slot 1: 10% clean
        // Slot 2: 20% clean
        // Slot 3: 90% clean (WIND)
        // Slot 4: 100% clean (SOLAR)
        // Algo for window 1h (2 slots), should pick 3 i 4 (average 95%).
        List<GenerationData> mockData = List.of(
                createData("2023-01-01T10:00Z", "2023-01-01T10:30Z", "gas", 90.0),
                createData("2023-01-01T10:30Z", "2023-01-01T11:00Z", "coal", 80.0),
                createData("2023-01-01T11:00Z", "2023-01-01T11:30Z", "wind", 90.0),
                createData("2023-01-01T11:30Z", "2023-01-01T12:00Z", "solar", 100.0)
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        ChargingWindowDto result = energyService.findBestChargingWindow(1);

        assertNotNull(result);
        assertEquals("2023-01-01T11:00Z", result.getStartTime());
        assertEquals("2023-01-01T12:00Z", result.getEndTime());
        assertEquals(95.0, result.getCleanEnergyPercent(), 0.01); // (90+100)/2 = 95%
    }

    @Test
    void shouldThrowException_WhenApiReturnsEmptyList() {
        // GIVEN
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(Collections.emptyList());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> energyService.findBestChargingWindow(1));
    }

    @Test
    void shouldThrowException_WhenNotEnoughDataForRequestedHours() {
        // GIVEN: 1h (2 slots)
        List<GenerationData> mockData = List.of(
                createData("2023-01-01T10:00Z", "2023-01-01T10:30Z", "wind", 50.0),
                createData("2023-01-01T10:30Z", "2023-01-01T11:00Z", "wind", 50.0)
        );
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN & THEN: ask 2h window
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> energyService.findBestChargingWindow(2));

        assertTrue(exception.getMessage().contains("Not enough data"));
    }

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