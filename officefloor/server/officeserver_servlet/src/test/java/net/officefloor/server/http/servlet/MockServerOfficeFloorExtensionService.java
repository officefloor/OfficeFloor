package net.officefloor.server.http.servlet;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.servlet.OfficeFloorFilterTest.Servicer;

/**
 * {@link OfficeExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeFloorExtensionService implements OfficeFloorExtensionService, OfficeExtensionService {

	/**
	 * Name of HTTP handling {@link OfficeSection}.
	 */
	public static final String HANDLER_SECTION_NAME = "section";

	/**
	 * Name of HTTP handling {@link OfficeSectionInput}.
	 */
	public static final String HANDLER_INPUT_NAME = "input";

	/*
	 * ======================= OfficeFloorExtensionService =======================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Obtain the input to service the HTTP requests
		DeployedOffice office = officeFloorDeployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
		DeployedOfficeInput officeInput = office.getDeployedOfficeInput(HANDLER_SECTION_NAME, HANDLER_INPUT_NAME);

		// Load the HTTP server
		HttpServer server = new HttpServer(officeInput, officeFloorDeployer, context);

		// Indicate the server
		System.out.println("HTTP server implementation " + server.getHttpServerImplementation().getClass().getName());
	}

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Enable wiring in the objects
		officeArchitect.enableAutoWireObjects();

		// Add section to service requests
		officeArchitect.addOfficeSection(HANDLER_SECTION_NAME, ClassSectionSource.class.getName(),
				Servicer.class.getName());
	}

}