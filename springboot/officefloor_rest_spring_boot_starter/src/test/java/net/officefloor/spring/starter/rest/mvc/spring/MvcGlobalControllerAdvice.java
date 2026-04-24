package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.MvcStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

/**
 * Plain {@link ControllerAdvice} (not {@link org.springframework.web.bind.annotation.RestControllerAdvice})
 * registering a global {@link InitBinder} that applies to all controllers.
 */
@ControllerAdvice
public class MvcGlobalControllerAdvice {

    @InitBinder
    public void configureBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MvcStatus.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(MvcStatus.valueOf(text.toUpperCase()));
            }
        });
    }
}
