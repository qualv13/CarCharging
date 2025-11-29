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
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ChargingServiceTest {

    @Mock
    private CarbonIntensityClient client;

    @InjectMocks
    private ChargingService chargingService;

    @Test
    void shouldCalculateBestWindowUsingRealApi() {
        RestTemplate realRestTemplate = new RestTemplate();
        CarbonIntensityClient realClient = new CarbonIntensityClient(realRestTemplate);
        ChargingService service = new ChargingService(realClient);

        // Test start
        System.out.println("--- Downloading data ---");

        // Sliding Window test
        ChargingWindowDto window = service.findBestChargingWindow(5);
        if (window != null) {
            System.out.println("Window found!");
            System.out.println("Start: " + window.getStartTime());
            System.out.println("End: " + window.getEndTime());
            System.out.println("Clean energy: " + window.getCleanEnergyPercent() + "%");
        } else {
            System.out.println("Failed to find window");
        }

        // Assertion checks
        Assertions.assertNotNull(window, "Window can't be null");
        Assertions.assertTrue(window.getCleanEnergyPercent() >= 0, "Percent can't be less than 0");
        Assertions.assertNotNull(window.getStartTime(), "Window start date must be provided");
    }

    @Test
    void shouldFindBestChargingWindow_PerfectData() {
        // for window 1h (2 slots), should pick 3 & 4 (average 95%).
        LocalDateTime baseTime = LocalDateTime.now().plusMinutes(5);

        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "gas", 90.0),
                createDynamicData(baseTime, 30, "coal", 80.0),
                createDynamicData(baseTime, 60, "wind", 90.0),
                createDynamicData(baseTime, 90, "solar", 100.0)
        );

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

        assertNotNull(result);
        String expectedStart = baseTime.plusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME);
        String expectedEnd = baseTime.plusMinutes(120).format(DateTimeFormatter.ISO_DATE_TIME);

        assertEquals(expectedStart, result.getStartTime());
        assertEquals(expectedEnd, result.getEndTime());

        assertEquals(95.0, result.getCleanEnergyPercent(), 0.1);
    }

    @Test
    void shouldThrowException_ApiReturnsEmptyList() {
        // GIVEN
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(Collections.emptyList());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> chargingService.findBestChargingWindow(1));
    }

    @Test
    void shouldThrowException_NotEnoughDataForRequestedHours() {
        // GIVEN: 1h (2 slots), we start in 5 min
        LocalDateTime baseTime = LocalDateTime.now().plusMinutes(5);

        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "wind", 50.0),
                createDynamicData(baseTime, 30, "wind", 50.0)
        );
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN & THEN: ask for 2h (4 slots), we have 2
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> chargingService.findBestChargingWindow(2));

        assertTrue(exception.getMessage().contains("Not enough future data"));
    }

    // Input validation
    @Test
    void shouldThrowException_HoursIsZeroOrNegative() {
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> chargingService.findBestChargingWindow(0));
        assertThrows(IllegalArgumentException.class, () -> chargingService.findBestChargingWindow(-1));
    }

    // exact size of window
    @Test
    void shouldReturnWindow_DataLengthEqualsRequestedWindow() {
        LocalDateTime baseTime = LocalDateTime.now().plusMinutes(5);

        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "wind", 100.0),
                createDynamicData(baseTime, 30, "wind", 100.0)
        );
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

        assertEquals(baseTime.format(DateTimeFormatter.ISO_DATE_TIME), result.getStartTime());
    }

    // check if returns earlier result when tie
    @Test
    void shouldSelectFirstWindow_TwoWindowsIdenticalScore() {
        LocalDateTime baseTime = LocalDateTime.now().plusMinutes(5);

        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "solar", 100.0),
                createDynamicData(baseTime, 30, "solar", 100.0),  // Clear 1h
                createDynamicData(baseTime, 60, "coal", 0.0),
                createDynamicData(baseTime, 90, "coal", 0.0),
                createDynamicData(baseTime, 120, "solar", 100.0),
                createDynamicData(baseTime, 150, "solar", 100.0)  // Clear 1h
        );
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

        // THEN
        assertNotNull(result);
        String expectedStart = baseTime.format(DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(expectedStart, result.getStartTime());
    }

    // best at the end
    @Test
    void shouldFindWindow_BestTimeAtTheEnd() {
        LocalDateTime baseTime = LocalDateTime.now().plusMinutes(5);

        List<GenerationData> mockData = List.of(
                createDynamicData(baseTime, 0, "coal", 0.0),
                createDynamicData(baseTime, 30, "coal", 0.0),
                createDynamicData(baseTime, 60, "wind", 100.0),
                createDynamicData(baseTime, 90, "wind", 100.0)
        );
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(mockData);

        // WHEN
        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

        // THEN
        String expectedStart = baseTime.plusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(expectedStart, result.getStartTime());
    }

    // API returns null
    @Test
    void shouldThrowException_ApiReturnsNull() {
        // GIVEN
        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(null);

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> chargingService.findBestChargingWindow(1));
    }

    // test with more complex data
    @Test
    void shouldCalculateCorrectly_MixedFuels() {

        FuelMix wind = new FuelMix("wind", 50.0);
        FuelMix solar = new FuelMix("solar", 30.0);
        FuelMix gas = new FuelMix("gas", 20.0);
        List<FuelMix> mix = List.of(wind, solar, gas);

        LocalDateTime now = LocalDateTime.now();
        String t0 = now.plusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME);
        String t1 = now.plusMinutes(40).format(DateTimeFormatter.ISO_DATE_TIME); // t0 + 30min
        String t2 = now.plusMinutes(70).format(DateTimeFormatter.ISO_DATE_TIME); // t1 + 30min

        GenerationData slot1 = new GenerationData(t0, t1, mix);
        GenerationData slot2 = new GenerationData(t1, t2, mix);

        Mockito.when(client.fetchGenerationData(anyString(), anyString())).thenReturn(List.of(slot1, slot2));

        // WHEN
        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

        // THEN
        assertEquals(80.0, result.getCleanEnergyPercent(), 0.01);
    }

//    private GenerationData createData(String from, String to, String fuelName, double perc) {
//        return new GenerationData(from, to, List.of(new FuelMix(fuelName, perc)));
//    }

    private GenerationData createDynamicData(LocalDateTime baseTime, int minutesOffset, String fuelName, double perc) {
        String from = baseTime.plusMinutes(minutesOffset).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = baseTime.plusMinutes(minutesOffset + 30).format(DateTimeFormatter.ISO_DATE_TIME);
        return new GenerationData(from, to, List.of(new FuelMix(fuelName, perc)));
    }
}
