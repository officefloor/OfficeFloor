package net.officefloor.server.http.servlet;

import javax.servlet.Filter;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;

/**
 * {@link HttpServerImplementation} to validate tests.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServletHttpServerImplementation implements HttpServerImplementation, OfficeExtensionService {

	/**
	 * {@link ThreadLocal} {@link Bridge}.
	 */
	private static final ThreadLocal<Bridge> threadLocalBridge = new ThreadLocal<>();

	/**
	 * Name of the {@link ThreadLocalAwareTeamSource} to provide synchronous
	 * blocking servicing to work within {@link Filter} chain.
	 */
	private static final String SYNC_TEAM_NAME = "_servlet_sync_team_";

	/**
	 * Provides context to load the {@link HttpServletHttpServerImplementation}.
	 */
	@FunctionalInterface
	public static interface HttpServletOfficeFloorLoader {

		/**
		 * Loads.
		 * 
		 * @throws Exception If fails to load.
		 */
		void load() throws Exception;
	}

	/**
	 * Bridge.
	 */
	private static class Bridge {

		/**
		 * {@link HttpServletOfficeFloorBridge} to return.
		 */
		private HttpServletOfficeFloorBridge bridge = null;
	}

	/**
	 * Loads the {@link HttpServletHttpServerImplementation}.
	 * 
	 * @param loader {@link HttpServletOfficeFloorLoader}.
	 * @return {@link HttpServletOfficeFloorBridge}.
	 * @throws Exception If fails to load.
	 */
	public static HttpServletOfficeFloorBridge load(HttpServletOfficeFloorLoader loader) throws Exception {
		Bridge bridge = new Bridge();
		try {
			threadLocalBridge.set(bridge);
			loader.load();
			return bridge.bridge;
		} finally {
			threadLocalBridge.remove();
		}
	}

	/*
	 * ===================== HttpServerImplementation ====================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	public void configureHttpServer(HttpServerImplementationContext context) {

		// Ensure within context
		Bridge bridge = threadLocalBridge.get();
		if (bridge == null) {
			throw new IllegalStateException("Must load " + this.getClass().getSimpleName() + " within load context");
		}

		// Obtain the required details
		HttpServerLocation location = context.getHttpServerLocation();
		boolean isIncludeEscalationStackTrace = context.isIncludeEscalationStackTrace();

		// Create the input
		ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input = context
				.getExternalServiceInput(ProcessAwareServerHttpConnectionManagedObject.class,
						ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());

		// Load the bridge
		bridge.bridge = new HttpServletOfficeFloorBridge(location, isIncludeEscalationStackTrace, input);

		// Register thread local aware team (to block invoking thread until serviced)
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorTeam team = deployer.addTeam(SYNC_TEAM_NAME, new ThreadLocalAwareTeamSource());

		// Register team to the office
		DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
		deployer.link(office.getDeployedOfficeTeam(SYNC_TEAM_NAME), team);
	}

	/*
	 * ===================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		/*
		 * Only extend if running within filter.
		 * 
		 * Note: running via MockWoofServerRule causes failure as:
		 * 
		 * - above configure HTTP server is not run (loading team)
		 * 
		 * - below team is configured without type for auto-wiring
		 * 
		 * - therefore, when teams added the below team fails the rule
		 */
		Bridge bridge = threadLocalBridge.get();
		if (bridge == null) {
			return;
		}

		// Add the team
		officeArchitect.addOfficeTeam(SYNC_TEAM_NAME);
	}

}