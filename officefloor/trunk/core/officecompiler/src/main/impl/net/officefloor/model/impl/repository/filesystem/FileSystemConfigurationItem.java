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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * File system {@link ConfigurationItem}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemConfigurationItem implements ConfigurationItem {

	/**
	 * Writes the configuration to the file.
	 * 
	 * @param file
	 *            File to contain the configuration.
	 * @param configuration
	 *            Configuration.
	 * @throws Exception
	 *             If fails to write configuration.
	 */
	static void writeConfiguration(File file, InputStream configuration)
			throws Exception {
		// Write the configuration
		FileOutputStream output = new FileOutputStream(file, false);
		int data;
		while ((data = configuration.read()) != -1) {
			output.write(data);
		}
		output.close();
	}

	/**
	 * File containing the configuration.
	 */
	private final File file;

	/**
	 * Location of this item.
	 */
	private final String location;

	/**
	 * {@link ConfigurationContext}.
	 */
	private ConfigurationContext configurationContext = null;

	/**
	 * Initiate.
	 * 
	 * @param file
	 *            File containing the configuration.
	 * @throws IOException
	 *             If fails to initialise.
	 */
	public FileSystemConfigurationItem(File file) throws IOException {
		this(file, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param file
	 *            File containing the configuration.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @throws IOException
	 *             If fails to initialise.
	 */
	public FileSystemConfigurationItem(File file,
			ConfigurationContext configurationContext) throws IOException {
		this(file.getCanonicalPath(), file, configurationContext);
	}

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Relative location of the {@link File} within the
	 *            {@link ConfigurationContext}.
	 * @param file
	 *            File containing the configuration.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 */
	public FileSystemConfigurationItem(String location, File file,
			ConfigurationContext configurationContext) {
		this.location = location;
		this.file = file;
		this.configurationContext = configurationContext;
	}

	/**
	 * Obtains the underlying {@link File}.
	 * 
	 * @return Underlying {@link File}.
	 */
	public File getFile() {
		return this.file;
	}

	/*
	 * ==================== ConfigurationItem ==================================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public ConfigurationContext getContext() {

		// Default to parent directory if no context
		if (this.configurationContext == null) {
			try {
				this.configurationContext = new FileSystemConfigurationContext(
						this.file.getParentFile());
			} catch (IOException ex) {
				throw new IllegalStateException(
						"Should always have a parent directory to file", ex);
			}
		}

		// Return the configuration context
		return this.configurationContext;
	}

	@Override
	public InputStream getConfiguration() throws Exception {
		return new FileInputStream(this.file);
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		writeConfiguration(this.file, configuration);
	}

}