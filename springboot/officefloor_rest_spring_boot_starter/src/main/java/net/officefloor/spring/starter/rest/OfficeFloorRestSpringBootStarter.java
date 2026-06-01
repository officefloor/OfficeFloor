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

package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import jakarta.annotation.PreDestroy;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import net.officefloor.woof.WoofLoaderSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles starting {@link OfficeFloor}.
 */
public class OfficeFloorRestSpringBootStarter {

    private final OfficeFloorRestProperties properties;

    private final ConfigurableApplicationContext applicationContext;

    private final ObjectMapper mapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints = new LinkedList<>();

    private final OpenAPI openApi = new OpenAPI();

    private HttpServletOfficeFloorBridge bridge;

    private OfficeFloor officeFloor;

    /**
     * Instantiate.
     *
     * @param properties         {@link OfficeFloorRestProperties}.
     * @param applicationContext {@link ConfigurableApplicationContext}.
     * @param mapper             {@link ObjectMapper}.
     */
    public OfficeFloorRestSpringBootStarter(OfficeFloorRestProperties properties,
                                            ConfigurableApplicationContext applicationContext,
                                            ObjectMapper mapper) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.mapper = mapper;

        // Set up OpenAPI
        this.openApi.setPaths(new Paths());
    }

    /**
     * Destroys the {@link OfficeFloor} on shutdown.
     *
     * @throws Exception If fails to close.
     */
    @PreDestroy
    public void destroy() throws Exception {
        if (this.officeFloor != null) {

            // Close and clean up
            this.officeFloor.closeOfficeFloor();
            this.officeFloor = null;
            this.bridge = null;
        }
    }

    /**
     * Ensures {@link OfficeFloor} is started.
     *
     * @throws Exception If fails to start the {@link OfficeFloor}.
     */
    public void startOfficeFloor() throws Exception {

        // Determine if already started
        if (this.officeFloor != null) {
            return; // already started
        }

        // Load OfficeFloor (capturing the REST endpoints)
        this.bridge = WoofLoaderSettings.contextualLoad((context) -> {

            // Avoid WoOF loading (as Spring specific)
            context.notLoad();

            // Load OfficeFloor
            return HttpServletHttpServerImplementation.load(() -> {

                // Compile the OfficeFloor
                OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
                compiler.setOfficeFloorSource(new SpringBootOfficeFloorSource(this.mapper, this.restEndpoints,
                        this.applicationContext, this.openApi));
                Map<String, String> sourceProperties = this.properties.getConfig();
                if (sourceProperties != null) {
                    sourceProperties.forEach(compiler::addProperty);
                }
                this.officeFloor = compiler.compile("OfficeFloor");
                this.officeFloor.openOfficeFloor();
            });
        });
    }

    /**
     * Obtains the {@link HttpServletOfficeFloorBridge}.
     *
     * @return {@link HttpServletOfficeFloorBridge}.
     */
    public HttpServletOfficeFloorBridge getBridge() {
        return this.bridge;
    }

    /**
     * Obtains the {@link OfficeFloorRestEndpoint} instances.
     *
     * @return {@link OfficeFloorRestEndpoint} instances.
     */
    public List<OfficeFloorRestEndpoint> getRestEndpoints() {
        return this.restEndpoints;
    }

    /**
     * Obtains the {@link OpenAPI}.
     *
     * @return {@link OpenAPI}.
     */
    public OpenAPI getOpenApi() {
        return this.openApi;
    }

}
