package com.rubinukperaj.weather.web.dto;

public record ErrorResponse(int status, String error, String message) {
}
