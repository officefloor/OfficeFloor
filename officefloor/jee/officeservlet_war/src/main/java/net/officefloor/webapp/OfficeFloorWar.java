/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.webapp;

import java.util.Arrays;

import net.officefloor.OfficeFloorMain;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.supply.ServletWoofExtensionService;

/**
 * Enables {@link OfficeFloor} to run a WAR application.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorWar extends OfficeFloorMain {

	/**
	 * {@link Property} name for the web application (WAR) path.
	 */
	public static final String PROPERTY_WAR_PATH = ApplicationOfficeFloorSource.OFFICE_NAME + "."
			+ ServletWoofExtensionService.PROPERTY_WAR_PATH;

	/**
	 * Run WAR.
	 * 
	 * @param args Arguments with last being path to WAR.
	 * @throws Exception If fails to run.
	 */
	public static void main(String... args) throws Exception {

		// Ensure odd number of arguments
		if ((args.length % 2) != 1) {
			throw new IllegalArgumentException(
					"USAGE: java " + OfficeFloorWar.class.getName() + " [property name/value pairs] <WAR path>");
		}

		// Split arguments
		String warPath = args[args.length - 1];
		String[] mainArgs = Arrays.copyOf(args, args.length - 1);

		// Run the application
		OfficeFloorMain.mainWithDefaults(new String[] { PROPERTY_WAR_PATH, warPath }, mainArgs);
	}

}
