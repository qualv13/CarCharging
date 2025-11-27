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