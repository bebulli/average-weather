package com.rubinukperaj.weather.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;

@Component
public class OpenWeatherMapClient implements WeatherProviderClient {

    private static final String BASE_URL = "https://api.openweathermap.org";

    private final RestClient restClient;
    private final String apiKey;

    public OpenWeatherMapClient(RestClient.Builder builder,
                                 @Value("${weather.openweathermap.api-key:}") String apiKey) {
        this.restClient = builder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
    }

    @Override
    public String name() {
        return "openweathermap";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public ProviderWeatherResult fetch(double lat, double lon) {
        if (!isConfigured()) {
            throw new ProviderException("openweathermap is not configured");
        }
        String uri = String.format(Locale.ROOT,
                "/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric", lat, lon, apiKey);
        try {
            OwmResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OwmResponse.class);
            if (response == null || response.main() == null) {
                throw new ProviderException("openweathermap returned an empty body");
            }
            String condition = (response.weather() != null && !response.weather().isEmpty())
                    ? capitalize(response.weather().get(0).description())
                    : "Unknown";
            double windKph = response.wind() != null ? response.wind().speed() * 3.6 : 0.0;
            return new ProviderWeatherResult(
                    name(),
                    response.main().temp(),
                    response.main().feelsLike(),
                    response.main().humidity(),
                    windKph,
                    condition);
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("openweathermap request failed", e);
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "Unknown";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OwmResponse(List<Weather> weather, Main main, Wind wind) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Weather(String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Main(
            double temp,
            @JsonProperty("feels_like") double feelsLike,
            double humidity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Wind(double speed) {
    }
}
