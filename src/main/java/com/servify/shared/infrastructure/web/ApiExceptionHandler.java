package com.servify.shared.infrastructure.web;

import com.servify.shared.domain.exception.BusinessRuleException;
import com.servify.shared.domain.exception.ForbiddenException;
import com.servify.shared.domain.exception.NotFoundException;
import com.servify.shared.domain.exception.UnauthorizedException;
import com.servify.shared.domain.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, exception, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler({
            ValidationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler({
            BusinessRuleException.class,
            IllegalStateException.class,
            UnsupportedOperationException.class
    })
    public ResponseEntity<ApiError> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception, request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, RuntimeException exception, HttpServletRequest request) {
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
