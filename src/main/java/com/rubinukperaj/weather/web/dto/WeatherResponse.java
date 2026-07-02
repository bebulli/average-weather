package com.rubinukperaj.weather.web.dto;

import java.time.Instant;
import java.util.List;

public record WeatherResponse(
        LocationDto location,
        double temperatureCelsius,
        double feelsLikeCelsius,
        double humidityPercent,
        double windSpeedKph,
        String condition,
        int sourcesAttempted,
        int sourcesUsed,
        List<ProviderReadingDto> providers,
        Instant fetchedAt) {
}
