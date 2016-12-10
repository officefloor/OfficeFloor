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
package net.officefloor.model.impl.repository.filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ReadOnlyConfigurationException;

/**
 * File system {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemConfigurationContext implements ConfigurationContext {

	/**
	 * Root directory for files containing the configuration.
	 */
	private final File rootDir;

	/**
	 * Location representing this file system repository.
	 */
	private final String location;

	/**
	 * Initiate.
	 * 
	 * @param rootDir
	 *            Root directory for files containing the configuration.
	 * @throws IOException
	 *             If fails to initiate.
	 */
	public FileSystemConfigurationContext(File rootDir) throws IOException {
		this.rootDir = rootDir;
		this.location = this.rootDir.getCanonicalPath();

		// Ensure the directory exists
		if (!this.rootDir.isDirectory()) {
			throw new FileNotFoundException("Configuration context directory '"
					+ this.rootDir.getPath() + "' not found");
		}
	}

	/*
	 * ================== ConfigurationContext ================================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public ConfigurationItem getConfigurationItem(String location)
			throws Exception {
		// Create the file
		File file = new File(this.rootDir, location);

		// Return the configuration item
		return new FileSystemConfigurationItem(location, file, this);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public ConfigurationItem createConfigurationItem(String location,
			InputStream configuration) throws Exception {

		// Create the file
		File file = new File(this.rootDir, location);

		// Ensure the parent directory exists
		file.getParentFile().mkdirs();

		// Write configuration to the file
		FileSystemConfigurationItem.writeConfiguration(file, configuration);

		// Create the configuration item
		return new FileSystemConfigurationItem(location, file, this);
	}

	@Override
	public void deleteConfigurationItem(String relativeLocation)
			throws Exception, ReadOnlyConfigurationException {

		// Obtain the file
		File file = new File(this.rootDir, relativeLocation);
		if (!(file.exists())) {
			return; // File not exists, so no need to delete it
		}

		// Delete the file
		if (!(file.delete())) {
			throw new IOException("Failed deleting file " + relativeLocation);
		}
	}

}