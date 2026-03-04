package net.officefloor.spring.starter.rest;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.server.http.HttpServer;
import net.officefloor.web.build.WebArchitect;
import org.slf4j.Logger;

public class SpringBootOfficeFloorSource extends AbstractOfficeFloorSource {

    private final Logger logger;

    public SpringBootOfficeFloorSource(Logger logger) {
        this.logger = logger;
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
        DeployedOffice deployedOffice = officeFloorDeployer.addDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME, new SpringBootOfficeSource(this, this.logger), "spring");

        // Provide default input for routing
        DeployedOfficeInput handlerInput = deployedOffice.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME);

        // Create the HTTP Server (using HttpServlet implementation)
        new HttpServer(handlerInput, officeFloorDeployer, officeFloorSourceContext);
    }

}
