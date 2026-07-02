package com.rubinukperaj.weather.service;

import com.rubinukperaj.weather.cache.WeatherCache;
import com.rubinukperaj.weather.exception.InvalidRequestException;
import com.rubinukperaj.weather.exception.NoWeatherDataException;
import com.rubinukperaj.weather.geocode.GeocodingService;
import com.rubinukperaj.weather.geocode.ResolvedLocation;
import com.rubinukperaj.weather.provider.ProviderWeatherResult;
import com.rubinukperaj.weather.provider.WeatherProviderClient;
import com.rubinukperaj.weather.reconcile.ReconciliationStrategy;
import com.rubinukperaj.weather.reconcile.WeatherSummary;
import com.rubinukperaj.weather.web.dto.LocationDto;
import com.rubinukperaj.weather.web.dto.ProviderReadingDto;
import com.rubinukperaj.weather.web.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class WeatherAggregationService {

    private final List<WeatherProviderClient> providers;
    private final GeocodingService geocodingService;
    private final ReconciliationStrategy reconciliationStrategy;
    private final WeatherCache<WeatherResponse> cache;
    private final ExecutorService executor;
    private final long futureTimeoutSeconds;

    public WeatherAggregationService(List<WeatherProviderClient> providers,
                                      GeocodingService geocodingService,
                                      ReconciliationStrategy reconciliationStrategy,
                                      WeatherCache<WeatherResponse> cache,
                                      ExecutorService providerExecutor,
                                      @Value("${weather.provider.future-timeout-seconds}") long futureTimeoutSeconds) {
        this.providers = providers;
        this.geocodingService = geocodingService;
        this.reconciliationStrategy = reconciliationStrategy;
        this.cache = cache;
        this.executor = providerExecutor;
        this.futureTimeoutSeconds = futureTimeoutSeconds;
    }

    public WeatherResponse getWeather(String city, Double lat, Double lon) {
        boolean hasCity = city != null && !city.isBlank();
        boolean hasCoords = lat != null && lon != null;

        if (hasCity == hasCoords) {
            throw new InvalidRequestException("Provide either 'city' or both 'lat' and 'lon', not neither or both");
        }

        LocationDto location = hasCity ? resolveByCity(city) : new LocationDto(coordinateLabel(lat, lon), lat, lon);
        String cacheKey = String.format(Locale.ROOT, "%.3f,%.3f", location.latitude(), location.longitude());

        Optional<WeatherResponse> cached = cache.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        List<ProviderWeatherResult> results = fetchAll(location.latitude(), location.longitude());
        if (results.isEmpty()) {
            throw new NoWeatherDataException(providers.size());
        }

        WeatherSummary summary = reconciliationStrategy.reconcile(results);
        WeatherResponse response = new WeatherResponse(
                location,
                summary.temperatureCelsius(),
                summary.feelsLikeCelsius(),
                summary.humidityPercent(),
                summary.windSpeedKph(),
                summary.condition(),
                providers.size(),
                results.size(),
                toProviderReadings(results),
                Instant.now());

        cache.put(cacheKey, response);
        return response;
    }

    private LocationDto resolveByCity(String city) {
        ResolvedLocation resolved = geocodingService.resolve(city);
        return new LocationDto(resolved.name(), resolved.latitude(), resolved.longitude());
    }

    private String coordinateLabel(double lat, double lon) {
        return String.format(Locale.ROOT, "%.4f,%.4f", lat, lon);
    }

    private List<ProviderWeatherResult> fetchAll(double lat, double lon) {
        List<CompletableFuture<ProviderWeatherResult>> futures = providers.stream()
                .map(provider -> CompletableFuture
                        .supplyAsync(() -> provider.fetch(lat, lon), executor)
                        .orTimeout(futureTimeoutSeconds, TimeUnit.SECONDS)
                        .exceptionally(ex -> null))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<ProviderReadingDto> toProviderReadings(List<ProviderWeatherResult> results) {
        return results.stream()
                .map(r -> new ProviderReadingDto(
                        r.provider(), round(r.temperatureCelsius()), round(r.feelsLikeCelsius()),
                        round(r.humidityPercent()), round(r.windSpeedKph()), r.condition()))
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
