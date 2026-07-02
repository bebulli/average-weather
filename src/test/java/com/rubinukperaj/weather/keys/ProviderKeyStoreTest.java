package com.rubinukperaj.weather.keys;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderKeyStoreTest {

    @Test
    void seedsFromConstructorDefaults() {
        ProviderKeyStore store = new ProviderKeyStore("owm-default", "");

        assertThat(store.hasOpenWeatherMapKey()).isTrue();
        assertThat(store.hasWeatherApiKey()).isFalse();
    }

    @Test
    void overridesAtRuntime() {
        ProviderKeyStore store = new ProviderKeyStore("", "");

        store.setWeatherApiKey("  new-key  ");

        assertThat(store.hasWeatherApiKey()).isTrue();
        assertThat(store.getWeatherApiKey()).isEqualTo("new-key");
    }

    @Test
    void blankOrNullKeyCountsAsNotConfigured() {
        ProviderKeyStore store = new ProviderKeyStore("initial", "");

        store.setOpenWeatherMapKey("   ");

        assertThat(store.hasOpenWeatherMapKey()).isFalse();
    }
}
