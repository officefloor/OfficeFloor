package net.officefloor.spring.starter.rest.thymeleaf;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ThymeLeafControllerAdvice {

    @ModelAttribute("hello")
    public String hello() {
        return "Hello";
    }

}
