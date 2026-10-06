package com.hackerrank.sample.exception;

import com.hackerrank.sample.dto.ApiErrorResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpError(org.springframework.web.ErrorResponseException exception) {
        return buildResponse(HttpStatus.valueOf(exception.getStatusCode().value()),
                HttpStatus.valueOf(exception.getStatusCode().value()).getReasonPhrase());
    }

    @ExceptionHandler({org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(Exception exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid or missing request parameter.");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMethod(org.springframework.web.HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .allow(exception.getSupportedHttpMethods().toArray(org.springframework.http.HttpMethod[]::new))
                .body(new ApiErrorResponse(405, "Method Not Allowed", "Request method not supported.", LocalDateTime.now()));
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(Exception exception) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported.");
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownRoute(Exception exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Route not found.");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String source = error instanceof org.springframework.validation.FieldError fieldError
                            ? fieldError.getField()
                            : error.getObjectName();
                    return source + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessageException(HttpMessageNotReadableException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body.");
    }

    @ExceptionHandler(BadResourceRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadResourceRequestException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoSuchResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoSuchResourceFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "No resource found for the provided id.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception) {
        log.error("Unexpected request failure", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred.");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now());

        return ResponseEntity.status(status).body(response);
    }
}
