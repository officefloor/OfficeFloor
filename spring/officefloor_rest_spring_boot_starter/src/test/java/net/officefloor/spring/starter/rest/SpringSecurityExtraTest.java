package net.officefloor.spring.starter.rest;

import net.officefloor.spring.starter.rest.security.SecuritySpringExceptionHandler;
import net.officefloor.spring.starter.rest.security.SecuritySpringExceptionHandlerServiceFactory;
import net.officefloor.spring.starter.rest.security.SpringSecurityOfficeExtension;
import net.officefloor.spring.starter.rest.security.SpringSecurityOfficeExtensionServiceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensure correct Spring Security configurations.
 */
public class SpringSecurityExtraTest {

    @Test
    public void springSecurityFilter() {
        assertEquals(SecurityFilterChain.class.getName(), SpringSecurityOfficeExtensionServiceFactory.SPRING_SECURITY_FILTER_CLASS_NAME);
    }

    @Test
    public void springSecurityOfficeExtension() {
        assertEquals(SpringSecurityOfficeExtension.class.getName(), SpringSecurityOfficeExtensionServiceFactory.SPRING_SECURITY_OFFICE_EXTENSION_CLASS_NAME);
    }

    @Test
    public void securitySpringExceptionHandler() {
        assertEquals(SecuritySpringExceptionHandler.class.getName(), SecuritySpringExceptionHandlerServiceFactory.SPRING_SECURITY_EXCEPTION_HANDLER_CLASS_NAME);
    }
}
