package net.officefloor.spring.starter.rest;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringMvcArgumentsTest {

    @Test
    public void ensureCorrectAuthenticationPrincipalClass() {
        assertEquals(AuthenticationPrincipal.class.getName(), SpringMvcArguments.AUTHENTICATION_PRINCIPAL_CLASS_NAME,
                "Ensure correct class name, as dynamically loaded");
    }

    @Test
    public void ensureCorrectAuthenticationClass() {
        assertEquals(Authentication.class.getName(), SpringMvcArguments.AUTHENTICATION_CLASS_NAME,
                "Ensure correct class name, as dynamically loaded");
    }
}
