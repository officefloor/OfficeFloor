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
package net.officefloor.web.resource.file;

import java.io.File;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.impl.AbstractHttpResource;

/**
 * {@link File} {@link HttpDirectory}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileHttpDirectory extends AbstractHttpResource implements HttpDirectory {

	/**
	 * Directory path.
	 */
	private String directoryPath;

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
	 *            WAR identifier to locate {@link FileHttpResourceFactory}.
	 * @param directory
	 *            Directory {@link File}.
	 * @param defaultFileNames
	 *            Names of the default {@link HttpFile} instances in order of
	 *            searching for the default {@link HttpFile}.
	 */
	public FileHttpDirectory(String resourcePath, File directory, String... defaultFileNames) {
		super(resourcePath.endsWith("/") ? resourcePath : resourcePath + "/");
		this.directoryPath = directory.getAbsolutePath();
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

		// Search for the default file
		for (String defaultFileName : this.defaultFileNames) {

			// Determine if default file exists
			File defaultFile = new File(this.directoryPath, defaultFileName);
			if (!(defaultFile.isFile())) {
				continue; // try next default file
			}

			// Have default file so create and return
			return (HttpFile) FileHttpResourceFactory.createHttpResource(defaultFile,
					this.resourcePath + defaultFileName);
		}

		// No default file
		return null;
	}

	@Override
	public HttpResource[] listResources() {

		// Create the listing of resources
		File[] files = new File(this.directoryPath).listFiles();
		HttpResource[] resources = new HttpResource[files.length];
		for (int i = 0; i < resources.length; i++) {
			File file = files[i];
			resources[i] = FileHttpResourceFactory.createHttpResource(file, this.resourcePath + file.getName());
		}

		// Return the resources
		return resources;
	}

}