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

@AutoConfiguration
@EnableConfigurationProperties(OfficeFloorRestProperties.class)
@ConditionalOnProperty(prefix = "officefloor.rest", name="enabled", havingValue = "true", matchIfMissing = true)
public class OfficeFloorRestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OfficeFloorRestSpringBootStarter getOfficeFloorRestSpringBootStarter(OfficeFloorRestProperties properties,
                                                                                ConfigurableApplicationContext applicationContext,
                                                                                ObjectMapper mapper) {
        return new OfficeFloorRestSpringBootStarter(properties, applicationContext, mapper);
    }

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
