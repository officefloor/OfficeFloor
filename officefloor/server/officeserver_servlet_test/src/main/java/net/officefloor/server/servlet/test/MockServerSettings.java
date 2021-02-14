/*-
 * #%L
 * Provides testing using HttpServlet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;

/**
 * Settings for running tests.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerSettings {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	static OfficeExtensionService officeExtensionService = null;

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

}
