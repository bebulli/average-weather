package com.rubinukperaj.weather.reconcile;

import com.rubinukperaj.weather.provider.ProviderWeatherResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AverageReconciliationStrategyTest {

    private final AverageReconciliationStrategy strategy = new AverageReconciliationStrategy();

    @Test
    void averagesNumericFieldsAcrossProviders() {
        List<ProviderWeatherResult> results = List.of(
                new ProviderWeatherResult("open-meteo", 20.0, 19.0, 50.0, 10.0, "Clear sky"),
                new ProviderWeatherResult("openweathermap", 22.0, 21.0, 60.0, 14.0, "Sunny"),
                new ProviderWeatherResult("weatherapi", 24.0, 23.0, 55.0, 12.0, "Clear")
        );

        WeatherSummary summary = strategy.reconcile(results);

        assertThat(summary.temperatureCelsius()).isEqualTo(22.0);
        assertThat(summary.feelsLikeCelsius()).isEqualTo(21.0);
        assertThat(summary.humidityPercent()).isEqualTo(55.0);
        assertThat(summary.windSpeedKph()).isEqualTo(12.0);
    }

    @Test
    void picksConditionFromProviderClosestToAverageTemperature() {
        List<ProviderWeatherResult> results = List.of(
                new ProviderWeatherResult("open-meteo", 10.0, 9.0, 50.0, 5.0, "Cold snap"),
                new ProviderWeatherResult("openweathermap", 21.0, 20.0, 50.0, 5.0, "Mild"),
                new ProviderWeatherResult("weatherapi", 20.0, 19.0, 50.0, 5.0, "Partly cloudy")
        );

        WeatherSummary summary = strategy.reconcile(results);

        assertThat(summary.condition()).isEqualTo("Partly cloudy");
    }

    @Test
    void roundsToOneDecimalPlace() {
        List<ProviderWeatherResult> results = List.of(
                new ProviderWeatherResult("open-meteo", 20.11, 19.0, 50.0, 10.0, "Clear"),
                new ProviderWeatherResult("openweathermap", 20.16, 19.0, 50.0, 10.0, "Clear")
        );

        WeatherSummary summary = strategy.reconcile(results);

        assertThat(summary.temperatureCelsius()).isEqualTo(20.1);
    }

    @Test
    void rejectsEmptyInput() {
        assertThatThrownBy(() -> strategy.reconcile(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
