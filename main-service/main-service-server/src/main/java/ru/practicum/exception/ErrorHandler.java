package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.error("Not found: {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("The required object was not found.")
                .status("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.error("Conflict: {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());

        String message = "Integrity constraint has been violated.";
        if (e.getMessage().contains("uq_email")) {
            message = "User with this email already exists";
        } else if (e.getMessage().contains("uq_category_name")) {
            message = "Category with this name already exists";
        }

        return ApiError.builder()
                .message(message)
                .reason("Integrity constraint has been violated.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());

        String message = String.format("Failed to convert value '%s' to required type '%s'",
                e.getValue(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

        return ApiError.builder()
                .message(message)
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", errors);
        return ApiError.builder()
                .message(errors)
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException e) {
        log.error("Bad request: {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneral(Exception e) {
        log.error("Internal error: {}", e.getMessage(), e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Internal server error.")
                .status("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }
}