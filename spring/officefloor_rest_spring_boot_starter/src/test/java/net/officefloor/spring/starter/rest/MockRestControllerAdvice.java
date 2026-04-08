package net.officefloor.spring.starter.rest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleDataIntegrityViolation() {
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleOptimisticLockException() {
    }

    @ExceptionHandler(MockRestController.CheckedRollbackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleCheckedRollbackException() {
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
