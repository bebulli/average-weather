package com.rubinukperaj.weather.reconcile;

import com.rubinukperaj.weather.provider.ProviderWeatherResult;

import java.util.List;

public interface ReconciliationStrategy {
    WeatherSummary reconcile(List<ProviderWeatherResult> results);
}
