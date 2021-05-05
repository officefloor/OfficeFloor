/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor.style;

import java.util.Arrays;

/**
 * System based {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemStyleRegistry extends AbstractStyleRegistry {

	/**
	 * Instantiate.
	 */
	public SystemStyleRegistry() {

		// Obtain the package path for the URL protocol
		String protocolPackageFull = Handler.class.getPackage().getName();
		final String protocolPackageName = protocolPackageFull.substring(0, protocolPackageFull.lastIndexOf('.'));

		// Determine if already include the package
		final String PROPERTY_NAME = "java.protocol.handler.pkgs";
		String existingPackages = System.getProperty(PROPERTY_NAME, "");

		// Determine if already contains package
		String[] packages = existingPackages.split("|");
		if (!Arrays.stream(packages).anyMatch((packageName) -> packageName.equals(protocolPackageName))) {

			// Load in the protocol package
			String newPackages = (((existingPackages == null) || (existingPackages.trim().length() == 0)) ? ""
					: existingPackages + "|") + protocolPackageName;
			System.setProperty(PROPERTY_NAME, newPackages);
		}
	}

}
