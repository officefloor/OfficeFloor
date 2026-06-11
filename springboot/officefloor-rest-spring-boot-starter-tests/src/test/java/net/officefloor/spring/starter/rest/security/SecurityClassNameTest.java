/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
