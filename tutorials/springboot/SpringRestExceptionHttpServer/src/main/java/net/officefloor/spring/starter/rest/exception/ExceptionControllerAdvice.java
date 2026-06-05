package net.officefloor.spring.starter.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// START SNIPPET: tutorial
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMockException(MockException ex) {
        return "Spring handled: " + ex.getMessage();
    }
}
// END SNIPPET: tutorial
