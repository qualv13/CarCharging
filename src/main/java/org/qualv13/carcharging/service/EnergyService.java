package org.qualv13.carcharging.service;

import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final CarbonIntensityClient client;

    private static final Set<String> CLEAN_SOURCES = Set.of("biomass", "nuclear", "hydro", "wind", "solar");

    public EnergyService(CarbonIntensityClient client) {
        this.client = client;
    }

    // Dane do wykresów
    public List<DailyMixDto> getEnergyMixForComingDays() {
        // 1. Pobierz dane na 3 dni (Client)
        LocalDate today = LocalDate.now();
        List<GenerationData> data = client.fetchGenerationData(today.toString(), today.plusDays(3).toString());
        // 2. Zgrupuj dane po dniu (Java Streams)
        if (data.isEmpty() || data == null) {
            return Collections.emptyList();
        }

        Map<LocalDate, List<GenerationData>> groupedByDate = data.stream()
                .collect(Collectors.groupingBy(this::parseDateFromInterval));

        List<DailyMixDto> result = new ArrayList<>();
        // 3. Policz średnie dla każdego paliwa
        for (Map.Entry<LocalDate, List<GenerationData>> entry : groupedByDate.entrySet()) {
            DailyMixDto dailyMixDto = calculateDailyAverage(entry.getKey(), entry.getValue());
            result.add(dailyMixDto);
        }
        // 4. Zwróć listę DTO
        return result; // TODO: Implementacja
    }

    private LocalDate parseDateFromInterval(GenerationData generationData) {
        return Instant.parse(generationData.getFrom()).atZone(ZoneId.of("UTC")).toLocalDate();
    }

    private DailyMixDto calculateDailyAverage(LocalDate key, List<GenerationData> value) {
        Map<String, Double> dailyMix = new HashMap<>();
        for (GenerationData generationData : value) {
            for (FuelMix source : value.getGenerationmix()) {

            }
        }
    }

    // ZADANIE B: Algorytm Smart Charging
    public ChargingWindowDto findBestChargingWindow(int hours) {
        // 1. Pobierz dane prognozowane (Client)
        // 2. Użyj algorytmu "Sliding Window" (ten z poprzedniej mojej odpowiedzi)
        // 3. Znajdź okno z najwyższą średnią
        return null; // TODO: Implementacja
    }
}
