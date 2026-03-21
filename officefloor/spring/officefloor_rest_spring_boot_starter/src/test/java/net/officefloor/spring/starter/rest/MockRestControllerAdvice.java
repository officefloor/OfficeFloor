package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MockRestControllerAdvice {

    /**
     * Mock {@link Exception}.
     */
    public static class MockException extends Exception {
        public MockException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(MockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMockException(MockException ex, HttpServletRequest request) {
        return request.getRequestURI() + ": " + ex.getMessage();
    }

}
