package org.ethanhao.triprover.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.exception.AuthOperationException;
import org.ethanhao.triprover.exception.PlanOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseResult<Object> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseResult<>(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResult<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseResult<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseResult<>(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() == null ? "Error" : error.getDefaultMessage(),
                        (first, second) -> first // Keep first error if duplicate keys
                ));
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        });
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
    }

    @ExceptionHandler(AuthOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleAuthOperationException(AuthOperationException ex) {
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(PlanOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handlePlanOperationException(PlanOperationException ex) {
        return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    // Generic exception handler for any unhandled exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResult<Object> handleAllUncaughtException(Exception ex) {
        // Log the exception here
        return new ResponseResult<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            "An unexpected error occurred: " + ex.getMessage()
        );
    }
} 