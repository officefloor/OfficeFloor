/*-
 * #%L
 * Model Generator
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * {@link ModelContext} that creates the {@link ModelFile} instances within a
 * file system relative to a root directory.
 *
 * @author Daniel Sagenschneider
 */
public class FileSystemModelContext implements ModelContext {

	/**
	 * Root directory.
	 */
	private final File rootDirectory;

	/**
	 * Initiate.
	 *
	 * @param rootDirectory
	 *            Root directory.
	 */
	public FileSystemModelContext(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	/*
	 * =================== ModelContext ===============================
	 */

	@Override
	public ModelFile createModelFile(String relativeLocation,
			InputStream contents) throws Exception {

		// Create the file
		File file = new File(this.rootDirectory, relativeLocation);

		// Ensure the parent directory exists
		file.getParentFile().mkdirs();

		// Write the configuration
		FileOutputStream output = new FileOutputStream(file, false);
		for (int data = contents.read(); data != -1; data = contents.read()) {
			output.write(data);
		}
		output.close();

		// Return the created Model File
		return new FileSystemModelFile(relativeLocation);
	}

	/**
	 * File system {@link ModelFile}.
	 */
	private class FileSystemModelFile implements ModelFile {

		/**
		 * Location.
		 */
		private final String location;

		/**
		 * Initiate.
		 *
		 * @param location
		 *            Location.
		 */
		public FileSystemModelFile(String location) {
			this.location = location;
		}

		/*
		 * =================== ModelFile ==============================
		 */

		@Override
		public String getLocation() {
			return this.location;
		}
	}

}
