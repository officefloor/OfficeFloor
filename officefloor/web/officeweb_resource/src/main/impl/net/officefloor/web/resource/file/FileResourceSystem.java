/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;

/**
 * {@link ResourceSystem} for files.
 * 
 * @author Daniel Sagenschneider
 */
public class FileResourceSystem implements ResourceSystem {

	/**
	 * Root directory for this {@link ResourceSystem}.
	 */
	private final Path rootDirectory;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 */
	public FileResourceSystem(ResourceSystemContext context) throws IOException {

		// Ensure have root directory for files
		this.rootDirectory = Paths.get(context.getLocation());
		if (!Files.isDirectory(this.rootDirectory)) {
			throw new FileNotFoundException("Can not find root directory for "
					+ FileResourceSystem.class.getSimpleName() + " at " + context.getLocation());
		}

		// TODO configure watching of directory for changes
	}

	/*
	 * =================== ResourceSystem =====================
	 */

	@Override
	public Path getResource(String path) throws IOException {

		// Need to strip off leading / to avoid absolute path resolution
		while (path.startsWith("/")) {
			path = path.substring("/".length());
		}

		// Return the path to potential file
		return this.rootDirectory.resolve(path);
	}

}