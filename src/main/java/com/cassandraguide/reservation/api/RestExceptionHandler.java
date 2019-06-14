package com.cassandraguide.reservation.api;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 *
 */
@ControllerAdvice(basePackages = {"com.cassandraguide.reservation"})
@Order(0)
public class RestExceptionHandler {
    
    /**
     * Catching all {@link IllegalArgumentException} error and convert to Bad request
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid Parameter")
    public void badRequestHandler() {}
    

}
