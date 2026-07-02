# average-weather

Aggregates current weather from three providers (Open-Meteo, OpenWeatherMap, WeatherAPI) and
returns a single reconciled reading. If a provider is slow, down, or not configured, it's dropped
from the result instead of failing the whole request.

Personal project. Uploaded to github.

Live: https://average-weather-production.up.railway.app/

## Running it

Needs Java 17 and Maven.

```
mvn spring-boot:run
```

Open http://localhost:8080. There's a small UI to enter OpenWeatherMap/WeatherAPI keys and look
up a city. Keys entered there live in server memory only for that run and are gone on restart.
Open-Meteo needs no key at all, so the app is useful with zero setup. If you'd rather not touch the
UI, the same two keys can be set as env vars before starting instead (`OWM_API_KEY`,
`WEATHERAPI_KEY`). Either path works, and the UI can override the env values at runtime.

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
errors, or isn't configured (missing API key) just gets excluded. The response still comes back
with whatever succeeded, plus `sourcesAttempted`/`sourcesUsed` so you can see how many actually
responded. The reconciliation step (`AverageReconciliationStrategy`) averages the numeric fields
across whatever providers returned, and picks the condition text from whichever provider's
temperature was closest to the average. It's swappable: `ReconciliationStrategy` is just an interface, so a
median-based version or something more elaborate can be dropped. Results are cached in memory per location
(rounded to ~100m) for 5 minutes, so repeat requests for the same city don't hit all three APIs again.
