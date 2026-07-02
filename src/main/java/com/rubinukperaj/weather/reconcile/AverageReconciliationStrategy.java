package com.rubinukperaj.weather.reconcile;

import com.rubinukperaj.weather.provider.ProviderWeatherResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AverageReconciliationStrategy implements ReconciliationStrategy {

    @Override
    public WeatherSummary reconcile(List<ProviderWeatherResult> results) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("cannot reconcile an empty result set");
        }

        double avgTemp = average(results, ProviderWeatherResult::temperatureCelsius);
        double avgFeelsLike = average(results, ProviderWeatherResult::feelsLikeCelsius);
        double avgHumidity = average(results, ProviderWeatherResult::humidityPercent);
        double avgWind = average(results, ProviderWeatherResult::windSpeedKph);
        String condition = mostRepresentativeCondition(results, avgTemp);

        return new WeatherSummary(
                round(avgTemp), round(avgFeelsLike), round(avgHumidity), round(avgWind), condition);
    }

    private double average(List<ProviderWeatherResult> results, java.util.function.ToDoubleFunction<ProviderWeatherResult> field) {
        return results.stream().mapToDouble(field).average().orElseThrow();
    }

    private String mostRepresentativeCondition(List<ProviderWeatherResult> results, double avgTemp) {
        ProviderWeatherResult closest = results.get(0);
        double smallestDiff = Math.abs(closest.temperatureCelsius() - avgTemp);
        for (ProviderWeatherResult result : results) {
            double diff = Math.abs(result.temperatureCelsius() - avgTemp);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                closest = result;
            }
        }
        return closest.condition();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
