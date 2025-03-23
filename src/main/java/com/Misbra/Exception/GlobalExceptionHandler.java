package com.Misbra.Exception;

import com.Misbra.Exception.Validation.ValidationErrorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    // Handle Spring validation errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ValidationErrorDTO> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::buildValidationError)
                .collect(Collectors.toList());

        return buildErrorResponse(
                "VALIDATION_FAILED",
                HttpStatus.BAD_REQUEST,
                errors,
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(
                "INVALID_ENUM_VALUE",
                HttpStatus.BAD_REQUEST,
                List.of(),
                request
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        // Log full error internally for debugging
        logger.error("Unhandled exception: ", ex);

        // Return a generic error to the client
        return buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                List.of(),
                request
        );
    }



    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(
            ValidationException ex, WebRequest request) {
        return buildErrorResponse(
                ex.getCode(),
                HttpStatus.BAD_REQUEST,
                ex.getErrors(),
                request
        );
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN,
                List.of(),
                request
        );
    }
    @ExceptionHandler(com.mongodb.MongoException.class)
    public ResponseEntity<Object> handleMongoException(
            com.mongodb.MongoException ex, WebRequest request) {
        return buildErrorResponse(
                "DATABASE_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                List.of(),
                request
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(
            NoSuchElementException ex, WebRequest request) {
        return buildErrorResponse(
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                List.of(),
                request
        );
    }


    // Generic error response builder
    private ResponseEntity<Object> buildErrorResponse(
            String code, HttpStatus status,
            List<ValidationErrorDTO> errors, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", resolveMessage(code, request));
        body.put("path", cleanRequestPath(request));
        body.put("errors", resolveErrorDetails(errors));

        return new ResponseEntity<>(body, status);
    }

    // Build validation error DTO from Spring FieldError
    private ValidationErrorDTO buildValidationError(FieldError fieldError) {
        List<String> params = new ArrayList<>();
        params.add(fieldError.getField()); // Field name as first parameter

        // Add non-resolvable arguments (constraint values)
        Arrays.stream(fieldError.getArguments())
                .filter(arg -> !(arg instanceof MessageSourceResolvable))
                .map(arg -> arg != null ? arg.toString() : "")
                .forEach(params::add);

        return new ValidationErrorDTO(
                fieldError.getCode(),
                params.toArray(new String[0])
        );
    }

    // Resolve bilingual error details
    private List<Map<String, Object>> resolveErrorDetails(List<ValidationErrorDTO> errors) {
        return errors.stream().map(error -> Map.of(
                "code", error.getCode(),
                "message", Map.of(
                        "en", resolveMessage(error.getCode(), error.getParams(), Locale.ENGLISH),
                        "ar", resolveMessage(error.getCode(), error.getParams(), new Locale("ar"))
                )
        )).collect(Collectors.toList());
    }

    // Message resolution utilities
    private String resolveMessage(String code, WebRequest request) {
        return messageSource.getMessage(
                code,
                null,
                "Validation failed",
                getRequestLocale(request)
        );
    }

    private String resolveMessage(String code, String[] params, Locale locale) {
        return messageSource.getMessage(
                code,
                params,
                locale.equals(Locale.ENGLISH)
                        ? "Validation error"
                        : "خطأ في التحقق",
                locale
        );
    }

    private Locale getRequestLocale(WebRequest request) {
        String langHeader = request.getHeader("Accept-Language");
        return (langHeader != null && !langHeader.isBlank())
                ? Locale.forLanguageTag(langHeader)
                : Locale.ENGLISH;
    }

    private String cleanRequestPath(WebRequest request) {
        return request.getDescription(false)
                .replace("uri=", "");
    }
}