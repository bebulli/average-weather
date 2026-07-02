package com.rubinukperaj.weather.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Locale;

@Component
public class OpenMeteoClient implements WeatherProviderClient {

    private static final String BASE_URL = "https://api.open-meteo.com";

    private final RestClient restClient;

    public OpenMeteoClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Override
    public String name() {
        return "open-meteo";
    }

    @Override
    public ProviderWeatherResult fetch(double lat, double lon) {
        String uri = String.format(Locale.ROOT,
                "/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code",
                lat, lon);
        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OpenMeteoResponse.class);
            if (response == null || response.current() == null) {
                throw new ProviderException("open-meteo returned an empty body");
            }
            Current current = response.current();
            return new ProviderWeatherResult(
                    name(),
                    current.temperature2m(),
                    current.apparentTemperature(),
                    current.relativeHumidity2m(),
                    current.windSpeed10m(),
                    WmoConditionMapper.describe(current.weatherCode()));
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("open-meteo request failed", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoResponse(Current current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Current(
            @JsonProperty("temperature_2m") double temperature2m,
            @JsonProperty("relative_humidity_2m") double relativeHumidity2m,
            @JsonProperty("apparent_temperature") double apparentTemperature,
            @JsonProperty("wind_speed_10m") double windSpeed10m,
            @JsonProperty("weather_code") int weatherCode) {
    }
}
