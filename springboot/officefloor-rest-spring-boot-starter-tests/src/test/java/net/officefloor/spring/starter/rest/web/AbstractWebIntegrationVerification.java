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

package net.officefloor.spring.starter.rest.web;

import net.officefloor.spring.starter.rest.AbstractVerification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration verification for OfficeFloor web features using a real HTTP server
 * (no MockMvc bean in context). Tests that confirm correct behaviour when Spring
 * annotations such as {@link org.springframework.web.bind.annotation.PathVariable}
 * and {@link org.springframework.web.bind.annotation.RequestParam} are used in
 * OfficeFloor service classes without {@link org.springframework.test.web.servlet.MockMvc}.
 */
public abstract class AbstractWebIntegrationVerification extends AbstractVerification {

    protected @Autowired TestRestTemplate client;

    /**
     * Confirms {@link org.springframework.web.bind.annotation.PathVariable} with String type
     * works via a real HTTP server (no MockMvc).
     */
    @Test
    public void pathVariableString() {
        ResponseEntity<String> response = client.getForEntity(this.getPath("/path/1"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ID=1", response.getBody());
    }

    /**
     * Confirms {@link org.springframework.web.bind.annotation.PathVariable} with Integer type
     * combined with a Spring bean works via a real HTTP server (no MockMvc).
     */
    @Test
    public void pathVariableInteger() {
        ResponseEntity<String> response = client.getForEntity(this.getPath("/path/int/42"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ID=42:COMPONENT", response.getBody());
    }

    /**
     * Confirms {@link org.springframework.web.bind.annotation.RequestParam} works via a real
     * HTTP server (no MockMvc).
     */
    @Test
    public void requestParam() {
        ResponseEntity<String> response = client.getForEntity(this.getPath("/query?name=value"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
    }

}
