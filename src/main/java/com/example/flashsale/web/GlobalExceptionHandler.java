package com.example.flashsale.web;

import com.example.flashsale.error.EventNotFoundException;
import com.example.flashsale.error.HoldExpiredException;
import com.example.flashsale.error.IdempotencyConflictException;
import com.example.flashsale.error.OrderConflictException;
import com.example.flashsale.error.OrderNotFoundException;
import com.example.flashsale.error.SoldOutException;
import com.example.flashsale.selfredis.SelfRedisException;
import com.example.flashsale.selfredis.SelfRedisUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({EventNotFoundException.class, OrderNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler({SoldOutException.class, OrderConflictException.class, IdempotencyConflictException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(HoldExpiredException.class)
    public ResponseEntity<ApiError> handleGone(HoldExpiredException e, HttpServletRequest request) {
        return build(HttpStatus.GONE, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "validation failed";
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(SelfRedisUnavailableException.class)
    public ResponseEntity<ApiError> handleSelfRedisDown(SelfRedisUnavailableException e, HttpServletRequest request) {
        log.error("Self-Redis unavailable: {}", e.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "hot-state store unavailable, please retry", request);
    }

    @ExceptionHandler(SelfRedisException.class)
    public ResponseEntity<ApiError> handleSelfRedis(SelfRedisException e, HttpServletRequest request) {
        log.error("Self-Redis error", e);
        return build(HttpStatus.BAD_GATEWAY, "hot-state store error", request);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
