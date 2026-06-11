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
 * Integration verification that endpoints remain accessible when
 * {@code server.servlet.context-path} is configured.
 * <p>
 * {@link TestRestTemplate} is auto-configured with a root URI that already
 * includes the context path.
 */
public abstract class AbstractWebContextPathIntegrationVerification extends AbstractVerification {

    static final String CONTEXT_PATH = "/context";

    @Autowired
    protected TestRestTemplate client;

    /**
     * Confirms that a basic endpoint is reachable when a context path is set.
     */
    @Test
    public void pathVariableString() {
        ResponseEntity<String> response = client.getForEntity(this.getPath("/path/1"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ID=1", response.getBody());
    }

    /**
     * Confirms that Spring {@code @RestControllerAdvice} exception handling works
     * when a context path is set.
     */
    @Test
    public void controllerAdvice() {
        String path = this.getPath("/controllerAdvice");
        ResponseEntity<String> response = client.getForEntity(path, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(CONTEXT_PATH + path + ": TEST", response.getBody());
    }

}
