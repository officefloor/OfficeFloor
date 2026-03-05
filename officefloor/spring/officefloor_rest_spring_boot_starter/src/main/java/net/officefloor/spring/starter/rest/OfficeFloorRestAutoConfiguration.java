package net.officefloor.spring.starter.rest;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(OfficeFloorRestProperties.class)
@ConditionalOnProperty(prefix = "officefloor.rest", name="enabled", havingValue = "true", matchIfMissing = true)
public class OfficeFloorRestAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeFloorRestAutoConfiguration.class.getName());

    @Bean
    @ConditionalOnMissingBean
    public OfficeFloorWebMvcConfigurer officeFloorWebMvcConfigurer(OfficeFloorRestProperties properties) throws Exception {

        // Load OfficeFloor (capturing the REST endpoints)
        List<OfficeFloorRestEndpoint> restEndpoints = new ArrayList<>();
        SpringBootOfficeFloorSource officeFloorSource = new SpringBootOfficeFloorSource(LOG, restEndpoints);
        HttpServletOfficeFloorBridge bridge = HttpServletHttpServerImplementation.load(() -> {

            // Compile the OfficeFloor
            OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
            compiler.setOfficeFloorSource(officeFloorSource);
            OfficeFloor officeFloor = compiler.compile("OfficeFloor");
            officeFloor.openOfficeFloor();
        });

        // Load the web configurer
        return new OfficeFloorWebMvcConfigurer(bridge, restEndpoints);
    }
}
