package com.rubinukperaj.weather.reconcile;

public record WeatherSummary(
        double temperatureCelsius,
        double feelsLikeCelsius,
        double humidityPercent,
        double windSpeedKph,
        String condition) {
}
