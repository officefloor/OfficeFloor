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
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeFloorExtensionService implements OfficeFloorExtensionService, OfficeExtensionService {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	private static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	private static OfficeExtensionService officeExtensionService = null;

	/**
	 * Logic to run within context.
	 */
	@FunctionalInterface
	public static interface WithinContext {
		void runInContext() throws Exception;
	}

	/**
	 * Runs {@link WithinContext} logic.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService}.
	 * @param officeExtension      {@link OfficeExtensionService}.
	 * @param logic                {@link WithinContext} logic.
	 * @throws Exception If failure with logic.
	 */
	public static void runWithinContext(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension, WithinContext logic) throws Exception {
		officeFloorExtensionService = officeFloorExtension;
		officeExtensionService = officeExtension;
		try {
			logic.runInContext();
		} finally {
			officeFloorExtensionService = null;
			officeExtensionService = null;
		}
	}

	/*
	 * ======================= OfficeFloorExtensionService =======================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (officeFloorExtensionService != null) {
			officeFloorExtensionService.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (officeExtensionService != null) {
			officeExtensionService.extendOffice(officeArchitect, context);
		}
	}

}
