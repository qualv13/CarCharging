package org.qualv13.carcharging.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class EnergyServiceIntegrationTest {

//    @Test
//    void shouldCalculateBestWindowUsingRealApi() {
//        // 1. Tworzymy prawdziwy "silnik" HTTP
//        RestTemplate realRestTemplate = new RestTemplate();
//
//        // 2. Tworzymy Twój klienta, który faktycznie uderzy do api.carbonintensity.org.uk
//        CarbonIntensityClient realClient = new CarbonIntensityClient(realRestTemplate);
//
//        // 3. Tworzymy serwis oparty na prawdziwym kliencie
//        EnergyService service = new EnergyService(realClient);
//
//        // --- TESTOWANIE ---
//
//        System.out.println("--- Rozpoczynam pobieranie danych z Wielkiej Brytanii... ---");
//
//        // Test 1: Sprawdźmy, czy algorytm znajdzie okno ładowania (np. 1-godzinne)
//        ChargingWindowDto window = service.findBestChargingWindow(1);
//
//        // Wypiszmy wynik w konsoli, żebyś widział, że to działa
//        if (window != null) {
//            System.out.println("Znaleziono najlepsze okno!");
//            System.out.println("Start: " + window.getStartTime());
//            System.out.println("Koniec: " + window.getEndTime());
//            System.out.println("Czysta energia: " + window.getCleanEnergyPercent() + "%");
//        } else {
//            System.out.println("Nie udało się wyznaczyć okna (czy API działa?)");
//        }
//
//        // Asercje (Warunki zaliczenia testu)
//        Assertions.assertNotNull(window, "Okno nie powinno być nullem - API powinno zwrócić dane");
//        Assertions.assertTrue(window.getCleanEnergyPercent() >= 0, "Procent nie może być ujemny");
//        Assertions.assertNotNull(window.getStartTime(), "Musi być data startu");
//    }

    @Test
    void shouldGetDailyMixUsingRealApi() {
        RestTemplate realRestTemplate = new RestTemplate();
        CarbonIntensityClient realClient = new CarbonIntensityClient(realRestTemplate);
        EnergyService service = new EnergyService(realClient);

        // Pobranie miksu na 3 dni
        List<DailyMixDto> mix = service.getEnergyMixForComingDays();

        System.out.println("--- Pobrano miks energetyczny na " + mix.size() + " dni ---");
        mix.forEach(day -> {
            System.out.println("Dzień: " + day.getDate() + ", Czysta energia: " + day.getCleanEnergyPercent() + "%");
        });

        Assertions.assertFalse(mix.isEmpty(), "Lista dni nie powinna być pusta");
        Assertions.assertEquals(3, mix.size(), "Powinniśmy dostać dane na 3 dni (Dziś, Jutro, Pojutrze)"); // Czasem API zwraca mniej pod koniec dnia, ale celujemy w 3
    }
}