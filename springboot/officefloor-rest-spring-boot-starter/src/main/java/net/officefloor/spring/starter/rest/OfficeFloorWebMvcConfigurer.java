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

import jakarta.annotation.PreDestroy;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import net.officefloor.web.rest.build.RestEndpoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** {@link WebMvcConfigurer} for OfficeFloor REST Spring Boot integration. */
public class OfficeFloorWebMvcConfigurer implements WebMvcConfigurer {

    private final OfficeFloorRestSpringBootStarter starter;

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    /** Instantiate.
     * @param starter {@link OfficeFloorRestSpringBootStarter}.
     * @param handlerAdapterProvider {@link RequestMappingHandlerAdapter} provider.
     * @param dispatcherServletProvider {@link DispatcherServlet} provider.
     * @param applicationContextProvider {@link ApplicationContext} provider.
     */
    public OfficeFloorWebMvcConfigurer(OfficeFloorRestSpringBootStarter starter,
                                       ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                       ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                       ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.starter = starter;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {

            // Ensure OfficeFloor started
            this.starter.startOfficeFloor();

            // Load the interceptors
            for (OfficeFloorRestEndpoint endpoint : this.starter.getRestEndpoints()) {
                registry.addInterceptor(
                                new OfficeFloorHandlerInterceptor(this.starter.getBridge(), endpoint,
                                        this.handlerAdapterProvider, this.dispatcherServletProvider,
                                        this.applicationContextProvider, this.starter.getSpringExceptionHandlers()))
                        .addPathPatterns(endpoint.getPath());
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        try {

            // Ensure OfficeFloor started
            this.starter.startOfficeFloor();

            // Load CORS
            for (OfficeFloorRestEndpoint endpoint : this.starter.getRestEndpoints()) {
                CorsConfiguration cors = endpoint.getCorsConfiguration();
                if (cors != null) {
                    registry.addMapping(endpoint.getPath()).combine(cors);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
