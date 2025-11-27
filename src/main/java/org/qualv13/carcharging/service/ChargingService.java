package org.qualv13.carcharging.service;

import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.ChargingWindowDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;
import org.qualv13.carcharging.util.EnergyConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ChargingService {

    private final CarbonIntensityClient client;

    public ChargingService(CarbonIntensityClient client) {
        this.client = client;
    }

    public ChargingWindowDto findBestChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("Charging window must be between 1 and 6");
        }
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

        double finalAverage = maxCleanSum / slotsNeeded;

        return new ChargingWindowDto(sortedData.get(bestStartIndex).getFrom(), sortedData.get(bestStartIndex + slotsNeeded - 1).getTo(), finalAverage);
    }

    private double calculateCleanEnergyPercentage(GenerationData generationData) {
        return generationData.getGenerationmix().stream()
                .filter(fuel -> EnergyConstants.CLEAN_SOURCES.contains(fuel.getFuel()))
                .mapToDouble(FuelMix::getPerc)
                .sum();
    }
}
