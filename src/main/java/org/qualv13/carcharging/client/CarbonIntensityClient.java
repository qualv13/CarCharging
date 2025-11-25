package org.qualv13.carcharging.client;

import org.qualv13.carcharging.model.external.GenerationData;
import org.qualv13.carcharging.model.external.CarbonApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class CarbonIntensityClient {

    private final RestTemplate restTemplate;
    private final String URL = "https://api.carbonintensity.org.uk/generation";

    public CarbonIntensityClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<GenerationData> fetchGenerationData(String fromDate, String toDate) {
        String final_url = URL + "/" + fromDate + "/" + toDate;
        CarbonApiResponse response = restTemplate.getForObject(final_url, CarbonApiResponse.class);
        return response != null ? response.getData() : List.of();
    }
}