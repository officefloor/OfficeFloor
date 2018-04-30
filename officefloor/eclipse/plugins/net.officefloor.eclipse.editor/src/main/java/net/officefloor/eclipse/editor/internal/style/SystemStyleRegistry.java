/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor.internal.style;

import net.officefloor.eclipse.editor.internal.officefloorstyle.Handler;

/**
 * System based {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemStyleRegistry extends AbstractStyleRegistry {

	static {
		// Obtain the package path for the URL protocol
		String protocolPackagePath = Handler.class.getPackage().getName();
		protocolPackagePath = protocolPackagePath.substring(0, protocolPackagePath.lastIndexOf('.'));

		// Load in the existing packages
		final String PROPERTY_NAME = "java.protocol.handler.pkgs";
		String existingPackages = System.getProperty(PROPERTY_NAME);
		String newPackages = (((existingPackages == null) || (existingPackages.trim().length() == 0)) ? ""
				: existingPackages + "|") + protocolPackagePath;
		System.setProperty(PROPERTY_NAME, newPackages);
	}

}