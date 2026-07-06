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

import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
     * Fallback {@link ObjectMapper} bean where Jackson 3.x is not auto-configured.
     * In SB4 this bean is never created because Spring Boot 4 auto-configures
     * {@code tools.jackson.databind.ObjectMapper} via its Jackson auto-configuration.
     * Uses {@code findAndRegisterModules()} so ServiceLoader-registered modules are picked up.
     *
     * @return {@link ObjectMapper}.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper officefloorJacksonObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    /**
     * Registers a {@code KotlinModule} bean when {@code jackson-module-kotlin} is on the
     * classpath. Installs Kotlin support on the OfficeFloor Jackson 3.x {@link ObjectMapper},
     * which is required for Jackson to deserialize Kotlin data classes (which have no no-arg
     * constructor).
     *
     * @return {@link JacksonModule} wrapping {@code KotlinModule}.
     * @throws Exception if reflection fails.
     */
    @Bean
    @ConditionalOnMissingBean(name = "kotlinJacksonModule")
    @ConditionalOnClass(name = "tools.jackson.module.kotlin.KotlinModule")
    public JacksonModule kotlinJacksonModule() throws Exception {
        Object builder = Class.forName("tools.jackson.module.kotlin.KotlinModule$Builder")
                .getDeclaredConstructor().newInstance();
        return (JacksonModule) builder.getClass().getMethod("build").invoke(builder);
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
