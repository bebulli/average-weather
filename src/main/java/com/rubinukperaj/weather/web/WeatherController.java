package com.rubinukperaj.weather.web;

import com.rubinukperaj.weather.service.WeatherAggregationService;
import com.rubinukperaj.weather.web.dto.WeatherResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherAggregationService aggregationService;

    public WeatherController(WeatherAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @Operation(summary = "Current weather for a city or a lat/lon pair, aggregated across providers")
    @GetMapping("/api/weather")
    public WeatherResponse getWeather(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon) {
        return aggregationService.getWeather(city, lat, lon);
    }
}
