package com.rubinukperaj.weather.exception;

public class NoWeatherDataException extends RuntimeException {
    public NoWeatherDataException(int attempted) {
        super("All " + attempted + " weather providers failed or were unavailable");
    }
}
