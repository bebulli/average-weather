package com.rubinukperaj.weather.web.dto;

public record ProviderReadingDto(
        String name,
        double temperatureCelsius,
        double feelsLikeCelsius,
        double humidityPercent,
        double windSpeedKph,
        String condition) {
}
