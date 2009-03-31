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
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * File system {@link net.net.officefloor.model.repository.ConfigurationContext}.
 * 
 * @author Daniel
 */
public class FileSystemConfigurationContext implements ConfigurationContext {

	/**
	 * Root directory for files containing the configuration.
	 */
	private final File rootDir;

	/**
	 * Id representing this file system repository.
	 */
	private final String id;

	/**
	 * Initiate.
	 * 
	 * @param rootDir
	 *            Root directory for files containing the configuration.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public FileSystemConfigurationContext(File rootDir) throws Exception {
		this.rootDir = rootDir;
		this.id = this.rootDir.getCanonicalPath();

		// Ensure the directory exists
		if (!this.rootDir.isDirectory()) {
			throw new FileNotFoundException("Configuration context directory '"
					+ this.rootDir.getPath() + "' not found");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getClasspath()
	 */
	public String[] getClasspath() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getConfigurationItem(java.lang.String)
	 */
	public ConfigurationItem getConfigurationItem(String id) throws Exception {
		// Create the file
		File file = new File(this.rootDir, id);

		// Return the configuration item
		return new FileSystemConfigurationItem(id, file, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#createConfigurationItem(java.lang.String,
	 *      java.io.InputStream)
	 */
	public ConfigurationItem createConfigurationItem(String id,
			InputStream configuration) throws Exception {
		// Create the file
		File file = new File(this.rootDir, id);

		// Ensure the parent directory exists
		file.getParentFile().mkdirs();

		// Write configuration to the file
		FileSystemConfigurationItem.writeConfiguration(file, configuration);

		// Create the configuration item
		return new FileSystemConfigurationItem(id, file, this);
	}

}
