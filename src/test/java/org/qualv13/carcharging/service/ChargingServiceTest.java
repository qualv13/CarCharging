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

        ChargingWindowDto result = chargingService.findBestChargingWindow(1);

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
        assertThrows(RuntimeException.class, () -> chargingService.findBestChargingWindow(1));
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
                () -> chargingService.findBestChargingWindow(2));

        assertTrue(exception.getMessage().contains("Not enough data"));
    }

    private GenerationData createData(String from, String to, String fuelName, double perc) {
        FuelMix fuelMix = new FuelMix();
        return new GenerationData(from, to, List.of(new FuelMix(fuelName, perc)));
    }
}
