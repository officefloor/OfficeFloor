package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestEndpointContext;
import net.officefloor.web.rest.build.RestEndpointListener;
import org.slf4j.Logger;

public class SpringBootOfficeSource extends AbstractOfficeSource {

    private final SpringBootOfficeFloorSource officeFloorSource;

    private final Logger logger;

    public SpringBootOfficeSource(SpringBootOfficeFloorSource officeFloorSource, Logger logger) {
        this.officeFloorSource = officeFloorSource;
        this.logger = logger;
    }

    /*
     * ======================= OfficeSource ========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) throws Exception {

        // Obtain the deployed office
        DeployedOffice office = this.officeFloorSource.getDeployedOffice();

        // Employ the architects
        WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(officeArchitect, officeSourceContext);
        ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
        RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

        // Configure object response
        webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(new ObjectMapper()));

        // Add the rest servicing
        this.logger.info("Loading REST endpoints:");
        PropertyList propertyList = officeSourceContext.createPropertyList();
        restArchitect.addRestServices(false, "officefloor/rest", propertyList, new RestEndpointListener() {
            @Override
            public void initialise(RestEndpointContext restEndpointContext) {
                logger.info("  " + restEndpointContext.getHttpMethod().getName() + " /" + restEndpointContext.getPath());
            }

            @Override
            public void endpoint(RestEndpoint restEndpoint) {

                // TODO determine office input configuration
                if (true) return;

                // Configure handling of rest end point
                DeployedOfficeInput officeInput = office.getDeployedOfficeInput("input", "input");
                ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> externalServiceInput = officeInput.addExternalServiceInput(ServerHttpConnection.class,
                        restEndpoint.getHttpMethod().getName() + "_" + restEndpoint.getPath(), ProcessAwareServerHttpConnectionManagedObject.class,
                        ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
            }
        });

        // Configure Office
        webArchitect.informOfficeArchitect();
    }

}
