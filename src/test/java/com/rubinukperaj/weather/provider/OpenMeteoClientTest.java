package com.rubinukperaj.weather.provider;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoClientTest {

    private static final String EXPECTED_URI = "https://api.open-meteo.com/v1/forecast?latitude=41.330000"
            + "&longitude=19.820000&current=temperature_2m,relative_humidity_2m,apparent_temperature,"
            + "wind_speed_10m,weather_code";

    @Test
    void parsesCurrentWeatherFromResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String body = """
                {
                  "current": {
                    "temperature_2m": 21.4,
                    "relative_humidity_2m": 58,
                    "apparent_temperature": 22.1,
                    "wind_speed_10m": 9.5,
                    "weather_code": 2
                  }
                }
                """;

        server.expect(requestTo(EXPECTED_URI))
                .andExpect(method(GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        OpenMeteoClient client = new OpenMeteoClient(builder);
        ProviderWeatherResult result = client.fetch(41.33, 19.82);

        assertThat(result.provider()).isEqualTo("open-meteo");
        assertThat(result.temperatureCelsius()).isEqualTo(21.4);
        assertThat(result.feelsLikeCelsius()).isEqualTo(22.1);
        assertThat(result.humidityPercent()).isEqualTo(58);
        assertThat(result.windSpeedKph()).isEqualTo(9.5);
        assertThat(result.condition()).isEqualTo("Partly cloudy");

        server.verify();
    }

    @Test
    void wrapsHttpFailuresInProviderException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo(EXPECTED_URI))
                .andExpect(method(GET))
                .andRespond(withServerError());

        OpenMeteoClient client = new OpenMeteoClient(builder);

        assertThatThrownBy(() -> client.fetch(41.33, 19.82))
                .isInstanceOf(ProviderException.class);
    }
}
