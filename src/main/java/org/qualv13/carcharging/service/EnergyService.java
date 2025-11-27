package org.qualv13.carcharging.service;

import org.qualv13.carcharging.client.CarbonIntensityClient;
import org.qualv13.carcharging.model.dto.DailyMixDto;
import org.qualv13.carcharging.model.external.FuelMix;
import org.qualv13.carcharging.model.external.GenerationData;
import org.qualv13.carcharging.util.EnergyConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final CarbonIntensityClient client;

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
            if(EnergyConstants.CLEAN_SOURCES.contains(entry.getKey())) {
                cleanEnergySum += avg;
            }
        }
        return new DailyMixDto(key.toString(), cleanEnergySum, dailyMixAvg);
    }

}
