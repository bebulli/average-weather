package com.rubinukperaj.weather.provider;

public record ProviderWeatherResult(
        String provider,
        double temperatureCelsius,
        double feelsLikeCelsius,
        double humidityPercent,
        double windSpeedKph,
        String condition) {
}
