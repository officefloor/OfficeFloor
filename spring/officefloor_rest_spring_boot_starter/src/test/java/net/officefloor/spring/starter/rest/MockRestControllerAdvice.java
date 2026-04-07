package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;

@RestControllerAdvice
public class MockRestControllerAdvice {

    @ModelAttribute("hello")
    public String hello() {
        return "Hello";
    }

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleDataIntegrityViolation() {
    }

    @InitBinder
    public void configureBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BindingTypes.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                this.setValue(BindingTypes.valueOf(text.toUpperCase()));
            }
        });
    }

    public static enum BindingTypes {
        START, COMPLETE
    }

}
