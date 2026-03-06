package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.server.http.HttpServer;
import net.officefloor.web.build.WebArchitect;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

public class SpringBootOfficeFloorSource extends AbstractOfficeFloorSource {

    private final Logger logger;

    private final ObjectMapper objectMapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    private final ConfigurableApplicationContext applicationContext;

    public SpringBootOfficeFloorSource(Logger logger,
                                       ObjectMapper objectMapper,
                                       List<OfficeFloorRestEndpoint> restEndpoints,
                                       ConfigurableApplicationContext applicationContext) {
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.restEndpoints = restEndpoints;
        this.applicationContext = applicationContext;
    }

    /*
     * ==================== OfficeFloorSource ====================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext officeFloorSourceContext) throws Exception {
        // No required properties
    }

    @Override
    public void sourceOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorSourceContext officeFloorSourceContext) throws Exception {

        // Enable auto-wiring
        officeFloorDeployer.enableAutoWireObjects();
        officeFloorDeployer.enableAutoWireTeams();

        // Configure web handling
        DeployedOffice deployedOffice = officeFloorDeployer.addDeployedOffice(
                ApplicationOfficeFloorSource.OFFICE_NAME,
                new SpringBootOfficeSource(this.logger, this.objectMapper, this.restEndpoints, this.applicationContext),
                "spring");
        for (String propertyName : officeFloorSourceContext.getPropertyNames()) {
            deployedOffice.addProperty(propertyName, officeFloorSourceContext.getProperty(propertyName));
        }

        // Provide default input for routing
        DeployedOfficeInput handlerInput = deployedOffice.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME);

        // Create the HTTP Server (using HttpServlet implementation)
        new HttpServer(handlerInput, officeFloorDeployer, officeFloorSourceContext);
    }

}
