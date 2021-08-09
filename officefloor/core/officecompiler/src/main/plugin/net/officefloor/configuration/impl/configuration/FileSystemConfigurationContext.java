/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.configuration.impl.configuration;

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

		}, (location, isCreate, configuration) -> {

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
