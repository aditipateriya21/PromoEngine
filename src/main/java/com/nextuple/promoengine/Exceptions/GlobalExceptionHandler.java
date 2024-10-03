package com.nextuple.promoengine.Exceptions;




import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


import java.util.HashMap;
import java.util.Map;

import static com.nextuple.promoengine.util.Constants.MESSAGE;
import static com.nextuple.promoengine.util.Constants.STATUS_CODE;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleOrderNotFoundException(OrderNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(STATUS_CODE, HttpStatus.NOT_FOUND.value());
        errorResponse.put(MESSAGE , ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleRuleNotFoundException(RuleNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(STATUS_CODE, HttpStatus.NOT_FOUND.value());
        errorResponse.put(MESSAGE , ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleInvalidInputException(InvalidInputException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(STATUS_CODE, HttpStatus.BAD_REQUEST.value());
        errorResponse.put(MESSAGE , ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuleFetchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleRuleFetchException(RuleFetchException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuleAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleRuleAlreadyExistsException(RuleAlreadyExistsException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put(MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }

}
