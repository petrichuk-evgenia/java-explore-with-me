package ru.practicum;

import lombok.Data;

@Data
public class ErrorResponse {
    private String error;
    private String message;
    private String path;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
