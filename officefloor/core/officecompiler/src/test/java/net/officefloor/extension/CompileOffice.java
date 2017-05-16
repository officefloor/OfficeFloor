/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.extension;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeExtensionService} to configure the {@link Office} within tests.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOffice implements OfficeExtensionService {

	/**
	 * {@link OfficeExtensionService} logic.
	 */
	private static OfficeExtensionService extender = null;

	/**
	 * Compiles the {@link Office}.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeExtensionService} to configure the
	 *            {@link Office}.
	 * @return {@link OfficeFloor}.
	 */
	public static OfficeFloor compileOffice(OfficeExtensionService officeConfiguration) {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Compile the solution
		try {
			extender = officeConfiguration;

			// Compile the office
			return compiler.compile(null);

		} finally {
			// Ensure the extender is cleared for other tests
			extender = null;
		}

	}

	/*
	 * ====================== OfficeExtensionService =====================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (extender != null) {
			extender.extendOffice(officeArchitect, context);
		}
	}

}