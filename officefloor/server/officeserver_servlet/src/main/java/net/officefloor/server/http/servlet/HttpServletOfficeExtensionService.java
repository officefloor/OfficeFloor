/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
