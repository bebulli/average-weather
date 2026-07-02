package com.rubinukperaj.weather.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String query) {
        super("No location found for '" + query + "'");
    }
}
