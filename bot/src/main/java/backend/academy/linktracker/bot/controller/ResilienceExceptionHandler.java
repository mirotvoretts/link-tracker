package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.response.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.response.ResponseBodyLiterals;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResilienceExceptionHandler {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RequestNotPermitted ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ResponseBodyLiterals.TOO_MANY_REQUESTS,
                "TOO_MANY_REQUESTS",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                null);
        return ResponseEntity.status(429).body(errorResponse);
    }
}
