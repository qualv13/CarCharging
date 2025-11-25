package org.qualv13.carcharging.service;

import ch.qos.logback.core.joran.sanity.Pair;
import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final CarbonIntensityClient client;

    private static final Set<String> CLEAN_SOURCES = Set.of("biomass", "nuclear", "hydro", "wind", "solar");

    public EnergyService(CarbonIntensityClient client) {
        this.client = client;
    }

    public List<DailyMixDto> getEnergyMixForComingDays() {
        LocalDate today = LocalDate.now();
        List<GenerationData> data = client.fetchGenerationData(today.toString(), today.plusDays(2).toString());
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, List<GenerationData>> groupedByDate = data.stream()
                .collect(Collectors.groupingBy(this::parseDateFromInterval));

        List<DailyMixDto> result = new ArrayList<>();
        // Average percent of fuels
        for (Map.Entry<LocalDate, List<GenerationData>> entry : groupedByDate.entrySet()) {
            DailyMixDto dailyMixDto = calculateDailyAverage(entry.getKey(), entry.getValue());
            result.add(dailyMixDto);
        }
        result.sort(Comparator.comparing(DailyMixDto::getDate));
        return result;
    }

    private LocalDate parseDateFromInterval(GenerationData item) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
        return LocalDateTime.parse(item.getFrom(), formatter).toLocalDate();
    }

    private DailyMixDto calculateDailyAverage(LocalDate key, List<GenerationData> value) {
        Map<String, Double> dailyMix = new HashMap<>();
        for (GenerationData interval : value) {
            for (FuelMix fuel : interval.getGenerationmix()) {
                dailyMix.merge(fuel.getFuel(), fuel.getPerc(), Double::sum);
            }
        }
        Map<String, Double> dailyMixAvg= new HashMap<>();
        double cleanEnergySum = 0;
        for (Map.Entry<String, Double> entry : dailyMix.entrySet()) {
            double avg = entry.getValue()/value.size();
            dailyMixAvg.put(entry.getKey(), avg);
            if(CLEAN_SOURCES.contains(entry.getKey())) {
                cleanEnergySum += avg;
            }
        }
        return new DailyMixDto(key.toString(), cleanEnergySum, dailyMixAvg);
    }

    public ChargingWindowDto findBestChargingWindow(int hours) {
        LocalDate today = LocalDate.now();
        List<GenerationData> data = client.fetchGenerationData(today.toString(), today.plusDays(1).toString());

        if (data.isEmpty()) {
            throw new RuntimeException("No API data provided");
        }

        List<GenerationData> sortedData = data.stream()
                .sorted(Comparator.comparing(GenerationData::getFrom))
                .toList();

        int slotsNeeded = hours * 2;

        if (sortedData.size() < slotsNeeded) {
            throw new IllegalArgumentException("Not enough data to calculate charging for " + hours + "h");
        }

        double maxCleanSum = -1.0;
        int bestStartIndex = -1;
        double currentWindowSum = 0.0;

        for (int i = 0; i < sortedData.size(); i++) {
            double cleanPercInSlot = calculateCleanEnergyPercentage(sortedData.get(i));
            currentWindowSum += cleanPercInSlot;

            if (i >= slotsNeeded) {
                double cleanPercLeaving = calculateCleanEnergyPercentage(sortedData.get(i - slotsNeeded));
                currentWindowSum -= cleanPercLeaving;
            }

            if (i >= slotsNeeded - 1) {
                if (currentWindowSum > maxCleanSum) {
                    maxCleanSum = currentWindowSum;
                    bestStartIndex = i - slotsNeeded + 1;
                }
            }
        }

        if (bestStartIndex == -1) {
            return null;
        }

        GenerationData startInterval = sortedData.get(bestStartIndex);
        double finalAverage = maxCleanSum / slotsNeeded;

        return new ChargingWindowDto(startInterval.getFrom(), finalAverage);
    }

    private double calculateCleanEnergyPercentage(GenerationData generationData) {
        return generationData.getGenerationmix().stream()
                .filter(fuel -> CLEAN_SOURCES.contains(fuel.getFuel()))
                .mapToDouble(FuelMix::getPerc)
                .sum();
    }

    private Map<String, Double> calculateHourByHourAverage(LocalDate key, List<GenerationData> value) {
        Map<String, Double> dailyMix = new HashMap<>();
        for (GenerationData interval : value) {
            for (FuelMix fuel : interval.getGenerationmix()) {
                if(CLEAN_SOURCES.contains(fuel.getFuel())) {
                    dailyMix.merge(interval.getFrom(), fuel.getPerc(), Double::sum);
                }
            }
        }
        return dailyMix;
    }
}
