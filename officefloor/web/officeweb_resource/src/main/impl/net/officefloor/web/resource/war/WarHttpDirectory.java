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
package net.officefloor.web.resource.war;

import java.io.File;

import net.officefloor.web.resource.AbstractHttpResource;
import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;

/**
 * WAR {@link HttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpDirectory extends AbstractHttpResource implements
		HttpDirectory {

	/**
	 * Identifier for {@link WarHttpResourceFactory}.
	 */
	private String warIdentifier;

	/**
	 * Directory.
	 */
	private File directory;

	/**
	 * Names of the default {@link HttpFile} instances in order of searching for
	 * the default {@link HttpFile}.
	 */
	private String[] defaultFileNames;

	/**
	 * Initiate.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @param warIdentifier
	 *            WAR identifier to locate {@link WarHttpResourceFactory}.
	 * @param directory
	 *            Directory {@link File}.
	 * @param defaultFileNames
	 *            Names of the default {@link HttpFile} instances in order of
	 *            searching for the default {@link HttpFile}.
	 */
	public WarHttpDirectory(String resourcePath, String warIdentifier,
			File directory, String... defaultFileNames) {
		super(resourcePath.endsWith("/") ? resourcePath : resourcePath + "/");
		this.warIdentifier = warIdentifier;
		this.directory = directory;
		this.defaultFileNames = defaultFileNames;
	}

	/*
	 * ====================== HttpDirectory =====================
	 */

	@Override
	public boolean isExist() {
		// Always exists
		return true;
	}

	@Override
	public HttpFile getDefaultFile() {

		// Obtain the HTTP Resource Factory
		WarHttpResourceFactory factory = WarHttpResourceFactory
				.getHttpResourceFactory(this.warIdentifier, null);

		// Search for the default file
		for (String defaultFileName : this.defaultFileNames) {

			// Determine if default file exists
			File defaultFile = new File(this.directory, defaultFileName);
			if (!(defaultFile.isFile())) {
				continue; // try next default file
			}

			// Have default file so create and return
			return (HttpFile) factory.createHttpResource(defaultFile,
					this.resourcePath + defaultFileName);
		}

		// No default file
		return null;
	}

	@Override
	public HttpResource[] listResources() {

		// Obtain the HTTP Resource Factory
		WarHttpResourceFactory factory = WarHttpResourceFactory
				.getHttpResourceFactory(this.warIdentifier, null);

		// Create the listing of resources
		File[] files = this.directory.listFiles();
		HttpResource[] resources = new HttpResource[files.length];
		for (int i = 0; i < resources.length; i++) {
			File file = files[i];
			resources[i] = factory.createHttpResource(file, this.resourcePath
					+ file.getName());
		}

		// Return the resources
		return resources;
	}

}