package net.officefloor.spring.starter.rest.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;

@RestControllerAdvice
public class MockWebRestControllerAdvice {

    @ExceptionHandler(MockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMockException(MockException ex, HttpServletRequest request) {
        return request.getRequestURI() + ": " + ex.getMessage();
    }

    @InitBinder
    public void configureBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MockBindingTypes.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                this.setValue(MockBindingTypes.valueOf(text.toUpperCase()));
            }
        });
    }

}
