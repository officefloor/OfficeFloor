package org.springframework.boot.test.autoconfigure.web.servlet;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shim for Spring Boot 4: forwards to the relocated AutoConfigureMockMvc annotation.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
public @interface AutoConfigureMockMvc {
}
