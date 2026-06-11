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

package net.officefloor.spring.starter.rest.openapi;

import net.officefloor.spring.starter.rest.OfficeFloorOpenApiConfiguration;
import net.officefloor.spring.starter.rest.OfficeFloorRestSpringBootStarter;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that both Spring-routed and OfficeFloor-routed endpoints are visible
 * in the OpenAPI specification, confirming seamless API surface integration.
 *
 * Spring endpoints are discovered automatically by SpringDoc via @RestController scanning.
 * OfficeFloor endpoints require OfficeFloor's SpringDoc integration to register them;
 * the officeFloorEndpointIncluded test will pass once that integration is implemented.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiTest {

    private @Autowired MockMvc mvc;

    @Test
    public void springEndpointIncluded() throws Exception {
        this.mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/spring/async/callable")));
    }

    @Test
    public void officeFloorEndpointIncluded() throws Exception {
        this.mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/officefloor/async/callable")));
    }

    @Test
    public void validateOptionalInclude() throws Exception {
        ConditionalOnClass conditionalOnClass = OfficeFloorOpenApiConfiguration.OptionalOpenApiConfiguration.class.getAnnotation(ConditionalOnClass.class);
        assertEquals(OpenApiCustomizer.class.getName(), conditionalOnClass.name()[0], "Ensure correct class name");
    }

}
