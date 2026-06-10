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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/** Auto-configuration for OfficeFloor REST integration. */
@AutoConfiguration
@EnableConfigurationProperties(OfficeFloorRestProperties.class)
@ConditionalOnProperty(prefix = "officefloor.rest", name="enabled", havingValue = "true", matchIfMissing = true)
public class OfficeFloorRestAutoConfiguration {

    /**
     * Fallback {@link ObjectMapper} bean for Spring Boot 4+, where Jackson 2.x is no longer
     * auto-configured (SB4 auto-configures tools.jackson.databind.ObjectMapper instead).
     * In SB3 this bean is never created because JacksonAutoConfiguration already provides one.
     *
     * @return {@link ObjectMapper}.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jackson2ObjectMapper() {
        return new ObjectMapper();
    }

    /**
     * Provides the {@link OfficeFloorRestSpringBootStarter} bean.
     *
     * @param properties         {@link OfficeFloorRestProperties}.
     * @param applicationContext {@link ConfigurableApplicationContext}.
     * @param mapper             {@link ObjectMapper}.
     * @return {@link OfficeFloorRestSpringBootStarter}.
     */
    @Bean
    @ConditionalOnMissingBean
    public OfficeFloorRestSpringBootStarter getOfficeFloorRestSpringBootStarter(OfficeFloorRestProperties properties,
                                                                                ConfigurableApplicationContext applicationContext,
                                                                                ObjectMapper mapper) {
        return new OfficeFloorRestSpringBootStarter(properties, applicationContext, mapper);
    }

    /**
     * Provides the {@link OfficeFloorWebMvcConfigurer} bean.
     *
     * @param starter                    {@link OfficeFloorRestSpringBootStarter}.
     * @param handlerAdapterProvider     {@link ObjectProvider} for {@link RequestMappingHandlerAdapter}.
     * @param dispatcherServletProvider  {@link ObjectProvider} for {@link DispatcherServlet}.
     * @param applicationContextProvider {@link ObjectProvider} for {@link ApplicationContext}.
     * @return {@link OfficeFloorWebMvcConfigurer}.
     * @throws Exception If fails to create the configurer.
     */
    @Bean
    public OfficeFloorWebMvcConfigurer officeFloorWebMvcConfigurer(
            OfficeFloorRestSpringBootStarter starter,
            ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
            ObjectProvider<DispatcherServlet> dispatcherServletProvider,
            ObjectProvider<ApplicationContext> applicationContextProvider) throws Exception {

        // Load the web configurer
        return new OfficeFloorWebMvcConfigurer(starter, handlerAdapterProvider,
                dispatcherServletProvider, applicationContextProvider);
    }

}
