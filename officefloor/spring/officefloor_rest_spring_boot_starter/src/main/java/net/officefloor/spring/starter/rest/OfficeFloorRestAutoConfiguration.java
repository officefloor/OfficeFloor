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
    public OfficeFloorWebMvcConfigurer officeFloorWebMvcConfigurer(OfficeFloorRestProperties properties, ObjectMapper mapper) throws Exception {

        // Load OfficeFloor (capturing the REST endpoints)
        OfficeFloor[] officeFloor = new OfficeFloor[1];
        List<OfficeFloorRestEndpoint> restEndpoints = new ArrayList<>();
        HttpServletOfficeFloorBridge bridge = HttpServletHttpServerImplementation.load(() -> {

            // Compile the OfficeFloor
            OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
            compiler.setOfficeFloorSource(new SpringBootOfficeFloorSource(LOG, mapper, restEndpoints));
            properties.getConfig().forEach(compiler::addProperty);
            officeFloor[0] = compiler.compile("OfficeFloor");
            officeFloor[0].openOfficeFloor();
        });

        // Load the web configurer
        return new OfficeFloorWebMvcConfigurer(officeFloor[0], bridge, restEndpoints);
    }
}
