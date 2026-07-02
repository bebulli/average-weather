package com.rubinukperaj.weather.web;

import com.rubinukperaj.weather.keys.ProviderKeyStore;
import com.rubinukperaj.weather.web.dto.KeyStatusResponse;
import com.rubinukperaj.weather.web.dto.SetKeysRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderKeyController {

    private final ProviderKeyStore keyStore;

    public ProviderKeyController(ProviderKeyStore keyStore) {
        this.keyStore = keyStore;
    }

    @Operation(summary = "Which providers currently have an API key set, without exposing the keys")
    @GetMapping("/api/config/keys")
    public KeyStatusResponse status() {
        return new KeyStatusResponse(keyStore.hasOpenWeatherMapKey(), keyStore.hasWeatherApiKey());
    }

    @Operation(summary = "Set provider API keys for this running instance (in memory only, lost on restart)")
    @PostMapping("/api/config/keys")
    public KeyStatusResponse setKeys(@RequestBody SetKeysRequest request) {
        if (request.openWeatherMapKey() != null) {
            keyStore.setOpenWeatherMapKey(request.openWeatherMapKey());
        }
        if (request.weatherApiKey() != null) {
            keyStore.setWeatherApiKey(request.weatherApiKey());
        }
        return status();
    }
}
