package com.rubinukperaj.weather.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rubinukperaj.weather.exception.LocationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GeocodingService {

    private static final String BASE_URL = "https://geocoding-api.open-meteo.com";

    private final RestClient restClient;

    public GeocodingService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    public ResolvedLocation resolve(String city) {
        String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
        GeocodeResponse response = restClient.get()
                .uri("/v1/search?name=" + encoded + "&count=1")
                .retrieve()
                .body(GeocodeResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new LocationNotFoundException(city);
        }

        Result result = response.results().get(0);
        return new ResolvedLocation(result.name(), result.latitude(), result.longitude());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodeResponse(List<Result> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Result(String name, double latitude, double longitude) {
    }
}
