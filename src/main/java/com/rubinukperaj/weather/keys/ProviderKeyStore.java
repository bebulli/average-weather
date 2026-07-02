package com.rubinukperaj.weather.keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class ProviderKeyStore {

    private final AtomicReference<String> openWeatherMapKey;
    private final AtomicReference<String> weatherApiKey;

    public ProviderKeyStore(@Value("${weather.openweathermap.api-key:}") String owmDefault,
                             @Value("${weather.weatherapi.api-key:}") String weatherApiDefault) {
        this.openWeatherMapKey = new AtomicReference<>(sanitize(owmDefault));
        this.weatherApiKey = new AtomicReference<>(sanitize(weatherApiDefault));
    }

    public String getOpenWeatherMapKey() {
        return openWeatherMapKey.get();
    }

    public String getWeatherApiKey() {
        return weatherApiKey.get();
    }

    public boolean hasOpenWeatherMapKey() {
        return !openWeatherMapKey.get().isBlank();
    }

    public boolean hasWeatherApiKey() {
        return !weatherApiKey.get().isBlank();
    }

    public void setOpenWeatherMapKey(String key) {
        openWeatherMapKey.set(sanitize(key));
    }

    public void setWeatherApiKey(String key) {
        weatherApiKey.set(sanitize(key));
    }

    private static String sanitize(String key) {
        return key == null ? "" : key.trim();
    }
}
