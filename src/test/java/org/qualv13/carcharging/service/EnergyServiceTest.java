package org.qualv13.carcharging.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        LocalDateTime baseTime = LocalDate.now().atTime(10, 0);

        // Expected average: 75% clean energy
        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "wind", 50.0),
                createDynamicData(baseTime, 240, "wind", 100.0)
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();

        // THEN
        assertEquals(1, result.size());
        assertEquals(baseTime.toLocalDate().toString(), result.get(0).getDate());
        assertEquals(75.0, result.get(0).getCleanEnergyPercent(), 0.01);
    }

    @Test
    void shouldGroupDataByDayCorrectly() {
        // Data across 2 different days (today and tomorrow).
        LocalDateTime todayBase = LocalDate.now().atTime(10, 0);
        LocalDateTime tomorrowBase = LocalDate.now().plusDays(1).atTime(10, 0);

        List<GenerationData> mockData = List.of(
                createDynamicData(todayBase, 0, "solar", 100.0),      // Day 1: Clean
                createDynamicData(tomorrowBase, 0, "coal", 100.0)     // Day 2: Dirty
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();

        // THEN
        assertEquals(2, result.size());

        // Validate Day 1
        DailyMixDto day1 = result.stream()
                .filter(d -> d.getDate().equals(todayBase.toLocalDate().toString()))
                .findFirst().orElseThrow();
        assertEquals(100.0, day1.getCleanEnergyPercent());

        // Validate Day 2
        DailyMixDto day2 = result.stream()
                .filter(d -> d.getDate().equals(tomorrowBase.toLocalDate().toString()))
                .findFirst().orElseThrow();
        assertEquals(0.0, day2.getCleanEnergyPercent());
    }

    @Test
    void shouldHandleMixedFuelsInOneSlot() {
        // single time slot containing multiple fuel types.
        FuelMix solar = new FuelMix("solar", 30.0);
        FuelMix wind = new FuelMix("wind", 20.0);
        FuelMix gas = new FuelMix("gas", 50.0);

        LocalDateTime now = LocalDateTime.now();
        String from = now.format(DateTimeFormatter.ISO_DATE_TIME) + "Z";
        String to = now.plusMinutes(30).format(DateTimeFormatter.ISO_DATE_TIME) + "Z";

        GenerationData mixedData = new GenerationData(from, to, List.of(solar, wind, gas));

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(List.of(mixedData));

        // WHEN
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();

        // THEN
        assertEquals(1, result.size());
        // 30 (solar) + 20 (wind) = 50% clean energy
        assertEquals(50.0, result.get(0).getCleanEnergyPercent(), 0.01);
    }

    @Test
    void shouldReturnEmptyList_WhenApiReturnsEmpty() {
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(Collections.emptyList());
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleNullResponse() {
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(Collections.emptyList());
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreArtifactsFromPreviousDay() {
        // The API sometimes returns the last slot of the previous day (yesterday 23:30 to today 0:00).
        LocalDateTime yesterdayLate = LocalDate.now().minusDays(1).atTime(23, 30);
        LocalDateTime todayEarly = LocalDate.now().atStartOfDay();

        List<GenerationData> mockData = List.of(
                createDynamicData(yesterdayLate, 0, "coal", 100.0),
                createDynamicData(todayEarly, 0, "wind", 100.0)
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        List<DailyMixDto> result = energyService.getEnergyMixForComingDays();

        // THEN
        assertEquals(1, result.size());
        assertEquals(LocalDate.now().toString(), result.get(0).getDate());
        // If the artifact was included, it would be 50%. Since it's ignored, it should be 100%.
        assertEquals(100.0, result.get(0).getCleanEnergyPercent(), 0.01);
    }

    // Helper function
    private GenerationData createDynamicData(LocalDateTime baseTime, int minutesOffset, String fuelName, double perc) {
        String from = baseTime.plusMinutes(minutesOffset).format(DateTimeFormatter.ISO_DATE_TIME) + "Z";
        String to = baseTime.plusMinutes(minutesOffset + 30).format(DateTimeFormatter.ISO_DATE_TIME) + "Z";
        return new GenerationData(from, to, List.of(new FuelMix(fuelName, perc)));
    }
}