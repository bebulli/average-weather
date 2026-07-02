package com.rubinukperaj.weather.provider;

public interface WeatherProviderClient {

    String name();

    ProviderWeatherResult fetch(double lat, double lon);

    default boolean isConfigured() {
        return true;
    }
}
