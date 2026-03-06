package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            ObjectMapper mapper) throws Exception {

        // Load the web configurer
        return new OfficeFloorWebMvcConfigurer(properties, applicationContext, mapper, LOG);
    }
}
