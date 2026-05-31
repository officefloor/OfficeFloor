package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import net.officefloor.activity.team.build.TeamDeployer;
import net.officefloor.activity.team.build.TeamEmployer;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.server.http.HttpServer;
import net.officefloor.web.build.WebArchitect;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/** {@link AbstractOfficeFloorSource} for Spring Boot. */
public class SpringBootOfficeFloorSource extends AbstractOfficeFloorSource {

    private final ObjectMapper objectMapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    private final ConfigurableApplicationContext applicationContext;

    private final OpenAPI openApi;

    /**
     * @param objectMapper       {@link ObjectMapper}.
     * @param restEndpoints      REST endpoints.
     * @param applicationContext {@link ConfigurableApplicationContext}.
     * @param openApi            {@link OpenAPI}.
     */
    public SpringBootOfficeFloorSource(ObjectMapper objectMapper,
                                       List<OfficeFloorRestEndpoint> restEndpoints,
                                       ConfigurableApplicationContext applicationContext,
                                       OpenAPI openApi) {
        this.objectMapper = objectMapper;
        this.restEndpoints = restEndpoints;
        this.applicationContext = applicationContext;
        this.openApi = openApi;
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
                new SpringBootOfficeSource(this.objectMapper, this.restEndpoints, this.applicationContext, this.openApi),
                "spring");
        for (String propertyName : officeFloorSourceContext.getPropertyNames()) {
            deployedOffice.addProperty(propertyName, officeFloorSourceContext.getProperty(propertyName));
        }

        // Load teams from officefloor/teams/ YAML files
        PropertyList teamProperties = officeFloorSourceContext.createPropertyList();
        for (String propertyName : officeFloorSourceContext.getPropertyNames()) {
            teamProperties.addProperty(propertyName).setValue(officeFloorSourceContext.getProperty(propertyName));
        }
        TeamDeployer teamDeployer = TeamEmployer.employTeamDeployer(officeFloorDeployer, officeFloorSourceContext, deployedOffice);
        teamDeployer.addTeams("officefloor/teams", teamProperties);

        // Provide default input for routing
        DeployedOfficeInput handlerInput = deployedOffice.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME);

        // Create the HTTP Server (using HttpServlet implementation)
        new HttpServer(handlerInput, officeFloorDeployer, officeFloorSourceContext);
    }

}
