# average-weather

Aggregates current weather from three providers (Open-Meteo, OpenWeatherMap, WeatherAPI) and
returns a single reconciled reading. If a provider is slow, down, or not configured, it's dropped
from the result instead of failing the whole request.

## Running it

Needs Java 17 and Maven. Open-Meteo works with no key. To also use OpenWeatherMap and WeatherAPI,
set these before starting:

```
export OWM_API_KEY=your_openweathermap_key
export WEATHERAPI_KEY=your_weatherapi_key
mvn spring-boot:run
```

Without the keys the app still runs fine, it just falls back to Open-Meteo alone.

Swagger UI: http://localhost:8080/swagger-ui/index.html

## Example

```
GET /api/weather?city=Tirana
```

```json
{
  "location": { "name": "Tirana", "latitude": 41.32744, "longitude": 19.81866 },
  "temperatureCelsius": 23.9,
  "feelsLikeCelsius": 25.4,
  "humidityPercent": 76.7,
  "windSpeedKph": 3.9,
  "condition": "Overcast clouds",
  "sourcesAttempted": 3,
  "sourcesUsed": 3,
  "providers": [
    { "name": "open-meteo", "temperatureCelsius": 22.8, "condition": "Partly cloudy", "..." : "..." },
    { "name": "openweathermap", "temperatureCelsius": 23.6, "condition": "Overcast clouds", "..." : "..." },
    { "name": "weatherapi", "temperatureCelsius": 25.3, "condition": "Patchy rain nearby", "..." : "..." }
  ],
  "fetchedAt": "2026-07-02T21:00:37Z"
}
```

You can also query by coordinates instead of a city name: `/api/weather?lat=41.33&lon=19.82`.

## How it works

A city name gets resolved to coordinates through Open-Meteo's geocoding endpoint, then all three
providers are called concurrently with a 2-3 second timeout each. Any provider that times out,
errors, or isn't configured (missing API key) just gets excluded — the response still comes back
with whatever succeeded, plus `sourcesAttempted`/`sourcesUsed` so you can see how many actually
responded. The reconciliation step (`AverageReconciliationStrategy`) averages the numeric fields
across whatever providers returned, and picks the condition text from whichever provider's
temperature was closest to the average, on the theory that its qualitative read is probably the
most representative one too. It's swappable — `ReconciliationStrategy` is just an interface, so a
median-based version or something more elaborate can be dropped in without touching the rest of
the app. Results are cached in memory per location (rounded to ~100m) for 5 minutes, so repeat
requests for the same city don't hit all three APIs again.
