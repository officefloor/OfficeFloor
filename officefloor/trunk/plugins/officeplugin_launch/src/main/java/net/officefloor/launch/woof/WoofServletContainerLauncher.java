/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.launch.woof;

import java.io.File;
import java.net.BindException;

import net.officefloor.compile.properties.PropertyList;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;

/**
 * WoOF {@link ServletContainerLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContainerLauncher extends ServletContainerLauncher {

	/**
	 * {@link File} name containing the {@link WoofDevelopmentConfiguration}
	 * within the WAR directory.
	 */
	public static final String CONFIGURATION_FILE_NAME = "woof-development-configuration.properties";

	/*
	 * ==================== ServletContainerLauncher =======================
	 */

	@Override
	public String getName() {
		return "OfficeFloor (WoOF)";
	}

	@Override
	public ServletContainer start(TreeLogger logger, int port, File appRootDir)
			throws BindException, Exception {

		// Load the WoOF configuration
		WoofDevelopmentConfiguration configuration = new WoofDevelopmentConfiguration(
				new File(appRootDir, CONFIGURATION_FILE_NAME));

		// Obtain the web app directory
		File webAppDirectory = configuration.getWebAppDirectory();

		// Include WAR directory in resource directories
		File[] configuredDirectories = configuration.getResourceDirectories();
		File[] resourceDirectories = new File[configuredDirectories.length + 1];
		resourceDirectories[0] = appRootDir;
		if (configuredDirectories.length > 0) {
			System.arraycopy(configuredDirectories, 0, resourceDirectories, 1,
					configuredDirectories.length);
		}

		// Obtain the properties
		PropertyList properties = configuration.getProperties();

		// Create and return the WoOF container
		return new WoofServletContainer(logger, this.getName(), port,
				webAppDirectory, resourceDirectories, properties);
	}

}