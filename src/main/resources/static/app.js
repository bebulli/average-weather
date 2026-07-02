"use strict";

const $ = (id) => document.getElementById(id);

async function api(method, path, body) {
  const opts = { method, headers: { "Content-Type": "application/json" } };
  if (body !== undefined) opts.body = JSON.stringify(body);
  const res = await fetch(path, opts);
  let data = null;
  try { data = await res.json(); } catch (e) { data = null; }
  if (!res.ok) {
    const msg = data && data.message ? data.message : "Request failed (" + res.status + ")";
    throw new Error(msg);
  }
  return data;
}

async function refreshKeyStatus() {
  try {
    const status = await api("GET", "/api/config/keys");
    const parts = [
      "OpenWeatherMap: " + (status.openWeatherMapConfigured ? "configured" : "not set"),
      "WeatherAPI: " + (status.weatherApiConfigured ? "configured" : "not set"),
    ];
    $("keyStatus").textContent = parts.join(" · ");
  } catch (e) {
    $("keyStatus").textContent = "Could not load key status: " + e.message;
  }
}

async function saveKeys() {
  const owmKey = $("owmKey").value.trim();
  const weatherApiKey = $("weatherApiKey").value.trim();
  try {
    await api("POST", "/api/config/keys", { openWeatherMapKey: owmKey, weatherApiKey: weatherApiKey });
    $("owmKey").value = "";
    $("weatherApiKey").value = "";
    await refreshKeyStatus();
  } catch (e) {
    $("keyStatus").textContent = e.message;
  }
}

function renderResult(data) {
  $("resultLocation").textContent = data.location.name;
  $("resultSources").textContent = data.sourcesUsed + " of " + data.sourcesAttempted + " sources used";
  $("resultTemp").textContent = data.temperatureCelsius + "°C";
  $("resultFeelsLike").textContent = data.feelsLikeCelsius + "°C";
  $("resultHumidity").textContent = data.humidityPercent + "%";
  $("resultWind").textContent = data.windSpeedKph + " km/h";
  $("resultCondition").textContent = data.condition;

  const list = $("providerList");
  list.innerHTML = "";
  data.providers.forEach((p) => {
    const div = document.createElement("div");
    div.className = "provider-line";
    div.innerHTML = "<div class='provider-name'>" + p.name + "</div>" +
        p.temperatureCelsius + "°C, feels like " + p.feelsLikeCelsius +
        "°C, " + p.humidityPercent + "% humidity, " + p.windSpeedKph +
        " km/h, " + p.condition;
    list.appendChild(div);
  });

  $("resultCard").classList.remove("hidden");
}

async function searchByCity() {
  $("searchError").textContent = "";
  const city = $("cityInput").value.trim();
  if (!city) {
    $("searchError").textContent = "Enter a city name.";
    return;
  }
  try {
    const data = await api("GET", "/api/weather?city=" + encodeURIComponent(city));
    renderResult(data);
  } catch (e) {
    $("searchError").textContent = e.message;
  }
}

async function searchByCoords() {
  $("searchError").textContent = "";
  const lat = $("latInput").value.trim();
  const lon = $("lonInput").value.trim();
  if (!lat || !lon) {
    $("searchError").textContent = "Enter both latitude and longitude.";
    return;
  }
  try {
    const data = await api("GET", "/api/weather?lat=" + encodeURIComponent(lat) + "&lon=" + encodeURIComponent(lon));
    renderResult(data);
  } catch (e) {
    $("searchError").textContent = e.message;
  }
}

function init() {
  $("saveKeys").onclick = saveKeys;
  $("searchBtn").onclick = searchByCity;
  $("searchCoordsBtn").onclick = searchByCoords;
  $("cityInput").addEventListener("keydown", (e) => { if (e.key === "Enter") searchByCity(); });
  refreshKeyStatus();
}

document.addEventListener("DOMContentLoaded", init);
