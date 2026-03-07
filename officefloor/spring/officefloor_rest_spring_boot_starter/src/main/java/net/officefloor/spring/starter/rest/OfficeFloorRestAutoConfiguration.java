package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@AutoConfiguration
@EnableConfigurationProperties(OfficeFloorRestProperties.class)
@ConditionalOnProperty(prefix = "officefloor.rest", name="enabled", havingValue = "true", matchIfMissing = true)
public class OfficeFloorRestAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeFloorRestAutoConfiguration.class.getName());

    @Bean
    @ConditionalOnMissingBean
    public OfficeFloorWebMvcConfigurer officeFloorWebMvcConfigurer(
            OfficeFloorRestProperties properties,
            ConfigurableApplicationContext applicationContext,
            ObjectMapper mapper,
            ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider) throws Exception {

        // Load the web configurer
        return new OfficeFloorWebMvcConfigurer(properties, applicationContext, mapper, LOG, handlerAdapterProvider);
    }
}
