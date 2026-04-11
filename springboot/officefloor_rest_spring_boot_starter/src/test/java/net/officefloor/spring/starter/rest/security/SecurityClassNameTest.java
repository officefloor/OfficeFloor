package net.officefloor.spring.starter.rest.security;

import net.officefloor.spring.starter.rest.argument.SpringMvcArguments;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensure correct Spring Security {@link Class} names.
 */
public class SecurityClassNameTest {

    @Test
    public void ensureCorrectAuthenticationPrincipalClass() {
        assertEquals(AuthenticationPrincipal.class.getName(), SpringMvcArguments.AUTHENTICATION_PRINCIPAL_CLASS_NAME);
    }

    @Test
    public void ensureCorrectAuthenticationClass() {
        assertEquals(Authentication.class.getName(), SpringMvcArguments.AUTHENTICATION_CLASS_NAME);
    }

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
