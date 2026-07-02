package com.rubinukperaj.weather.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Locale;

@Component
public class WeatherApiClient implements WeatherProviderClient {

    private static final String BASE_URL = "https://api.weatherapi.com";

    private final RestClient restClient;
    private final String apiKey;

    public WeatherApiClient(RestClient.Builder builder,
                             @Value("${weather.weatherapi.api-key:}") String apiKey) {
        this.restClient = builder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
    }

    @Override
    public String name() {
        return "weatherapi";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public ProviderWeatherResult fetch(double lat, double lon) {
        if (!isConfigured()) {
            throw new ProviderException("weatherapi is not configured");
        }
        String uri = String.format(Locale.ROOT,
                "/v1/current.json?key=%s&q=%f,%f", apiKey, lat, lon);
        try {
            WeatherApiResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(WeatherApiResponse.class);
            if (response == null || response.current() == null) {
                throw new ProviderException("weatherapi returned an empty body");
            }
            Current current = response.current();
            String condition = current.condition() != null ? current.condition().text() : "Unknown";
            return new ProviderWeatherResult(
                    name(),
                    current.tempC(),
                    current.feelslikeC(),
                    current.humidity(),
                    current.windKph(),
                    condition);
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("weatherapi request failed", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WeatherApiResponse(Current current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Current(
            @JsonProperty("temp_c") double tempC,
            @JsonProperty("feelslike_c") double feelslikeC,
            double humidity,
            @JsonProperty("wind_kph") double windKph,
            Condition condition) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Condition(String text) {
    }
}
