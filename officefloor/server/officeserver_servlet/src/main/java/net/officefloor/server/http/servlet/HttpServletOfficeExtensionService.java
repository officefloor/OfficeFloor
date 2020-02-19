package net.officefloor.server.http.servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link OfficeExtensionService} for the HTTP Servlet.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * ===================== OfficeExtensionService ======================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		/*
		 * Only extend if running within filter.
		 * 
		 * Note: running via MockWoofServerRule causes failure as:
		 * 
		 * - configure HTTP server is not run (loading team)
		 * 
		 * - below team is configured without type for auto-wiring
		 * 
		 * - therefore, when teams added the below team fails the rule
		 */
		if (!HttpServletHttpServerImplementation.isWithinOfficeFloorFilter()) {
			return;
		}

		// Add the team
		officeArchitect.addOfficeTeam(HttpServletHttpServerImplementation.SYNC_TEAM_NAME);
	}

}