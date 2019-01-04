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
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

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

		// Load the team marker and team
		Singleton.load(officeFloorDeployer, new TeamMarker(), office);
		officeFloorDeployer.addTeam("TEAM", OnePersonTeamSource.class.getName()).addTypeQualification(null,
				TeamMarker.class.getName());
	}

	public static class TeamMarker {
	}

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Enable auto-wiring
		officeArchitect.enableAutoWireObjects();
		officeArchitect.enableAutoWireTeams();

		// Add section to service requests
		OfficeSection router = officeArchitect.addOfficeSection(HANDLER_SECTION_NAME,
				ClassSectionSource.class.getName(), Router.class.getName());

		// Create the service handlers
		OfficeSection officeFloorHandler = officeArchitect.addOfficeSection("officefloor",
				ClassSectionSource.class.getName(), OfficeFloorFilterTest.Servicer.class.getName());
		OfficeSection officeFloorTeamHandler = officeArchitect.addOfficeSection("officefloorTeam",
				ClassSectionSource.class.getName(), OfficeFloorFilterTest.TeamServicer.class.getName());
		OfficeSection delayedFallbackHandler = officeArchitect.addOfficeSection("delayedFallback",
				ClassSectionSource.class.getName(), DelayedFallbackServicer.class.getName());

		// Wire servicing
		officeArchitect.link(router.getOfficeSectionOutput("service"),
				officeFloorHandler.getOfficeSectionInput("service"));
		officeArchitect.link(router.getOfficeSectionOutput("serviceTeams"),
				officeFloorTeamHandler.getOfficeSectionInput("teams"));
		officeArchitect.link(router.getOfficeSectionOutput("delayedFallback"),
				delayedFallbackHandler.getOfficeSectionInput("service"));
	}

	@FlowInterface
	public static interface Routes {
		void service();

		void serviceTeams();

		void delayedFallback();
	}

	/**
	 * {@link ClassSectionSource} to route requests.
	 */
	public static class Router {

		public static final String SINGLE_TEAM_PATH = "/single";

		public static final String MULTIPLE_TEAM_PATH = "/multiple";

		public static final String DELAYED_NOT_FOUND_PATH = "/delayed-fallback";

		public void input(ServerHttpConnection connection, Routes routes) {

			// Attempt to route request
			switch (connection.getRequest().getUri()) {
			case SINGLE_TEAM_PATH:
				routes.service();
				return;

			case MULTIPLE_TEAM_PATH:
				routes.serviceTeams();
				return;

			case DELAYED_NOT_FOUND_PATH:
				routes.delayedFallback();
				return;
			}

			// As here, not handled
			connection.getResponse().setStatus(HttpStatus.NOT_FOUND);
		}
	}

	public static class DelayedFallbackServicer {
		public void service(ServerHttpConnection connection, TeamMarker marker) {
			connection.getResponse().setStatus(HttpStatus.NOT_FOUND);
		}
	}

}