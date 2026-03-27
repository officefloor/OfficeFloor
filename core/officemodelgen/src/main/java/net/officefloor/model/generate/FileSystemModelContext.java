/*-
 * #%L
 * Model Generator
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
