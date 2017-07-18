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
package net.officefloor.configuration.impl.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.configuration.WritableConfigurationContext;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.AbstractWritableConfigurationContext;

/**
 * File system {@link WritableConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemConfigurationContext extends AbstractWritableConfigurationContext {

	/**
	 * Creates a {@link WritableConfigurationItem} for the {@link File}.
	 * 
	 * @param file
	 *            {@link File} for the {@link WritableConfigurationItem}.
	 * @return {@link WritableConfigurationItem}
	 * @throws IOException
	 *             If fails to create the {@link WritableConfigurationItem}.
	 */
	public static WritableConfigurationItem createWritableConfigurationItem(File file) throws IOException {
		return new FileSystemConfigurationContext(file.getParentFile()).getWritableConfigurationItem(file.getName());
	}

	/**
	 * Initiate.
	 * 
	 * @param rootDir
	 *            Root directory for files containing the configuration.
	 * @throws IOException
	 *             If fails to initiate.
	 */
	public FileSystemConfigurationContext(File rootDir) throws IOException {
		super((location) -> {

			// Obtain the configuration
			return new FileInputStream(new File(rootDir, location));

		}, (location, configuration) -> {

			// Write the configuration
			File file = new File(rootDir, location);

			// Ensure the parent directory exists
			file.getParentFile().mkdirs();

			// Write the content to the file
			OutputStream outputStream = new FileOutputStream(file);
			for (int byteValue = configuration.read(); byteValue >= 0; byteValue = configuration.read()) {
				outputStream.write(byteValue);
			}
			outputStream.close();

		}, (location) -> {

			// Obtain the file
			File file = new File(rootDir, location);
			if (!(file.exists())) {
				return; // File not exists, so no need to delete it
			}

			// Delete the file
			if (!(file.delete())) {
				throw new IOException("Failed deleting file " + location);
			}
		}, null);

		// Ensure the directory exists
		if (!rootDir.isDirectory()) {
			throw new FileNotFoundException("Configuration context directory '" + rootDir.getPath() + "' not found");
		}
	}

}