package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.ErrorResponse;
import net.officefloor.spring.starter.rest.mvc.common.MvcException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Demonstrates that {@link RestControllerAdvice} serialises the handler's return value as JSON.
 */
@RestControllerAdvice
public class MvcRestControllerAdvice {

    @ExceptionHandler(MvcException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleMvcException(MvcException ex) {
        return new ErrorResponse(ex.getCode(), ex.getMessage());
    }
}
