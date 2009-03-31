/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.repository.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * File system {@link net.net.officefloor.model.repository.ConfigurationItem}.
 * 
 * @author Daniel
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
	 * Id of this item.
	 */
	private final String id;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * Initiate.
	 * 
	 * @param file
	 *            File containing the configuration.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public FileSystemConfigurationItem(File file,
			ConfigurationContext configurationContext) throws Exception {
		this(file.getCanonicalPath(), file, configurationContext);
	}

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id (allows for relative path, rather than full file path).
	 * @param file
	 *            File containing the configuration.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 */
	protected FileSystemConfigurationItem(String id, File file,
			ConfigurationContext configurationContext) {
		this.id = id;
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
	 * ==============================================================================
	 * ConfigurationItem
	 * ==============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#getConfiguration()
	 */
	public InputStream getConfiguration() throws Exception {
		return new FileInputStream(this.file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#setConfiguration(java.io.InputStream)
	 */
	public void setConfiguration(InputStream configuration) throws Exception {
		writeConfiguration(this.file, configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#getContext()
	 */
	public ConfigurationContext getContext() {
		return this.configurationContext;
	}

}
