package org.qualv13.carcharging.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class EnergyServiceIntegrationTest {

    @Test
    void shouldCalculateBestWindowUsingRealApi() {
        RestTemplate realRestTemplate = new RestTemplate();
        CarbonIntensityClient realClient = new CarbonIntensityClient(realRestTemplate);
        EnergyService service = new EnergyService(realClient);

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
    void shouldGetDailyMixUsingRealApi() {
        RestTemplate realRestTemplate = new RestTemplate();
        CarbonIntensityClient realClient = new CarbonIntensityClient(realRestTemplate);
        EnergyService service = new EnergyService(realClient);

        List<DailyMixDto> mix = service.getEnergyMixForComingDays();

        System.out.println("--- Got energy mix for " + mix.size() + " days ---");
        mix.forEach(day -> {
            System.out.println("Day: " + day.getDate() + "\nClean energy: " + day.getCleanEnergyPercent() + "%");
        });

        Assertions.assertFalse(mix.isEmpty(), "List of days can't be empty");
        Assertions.assertEquals(3, mix.size(), "Number of days should be equal 3");
    }
}