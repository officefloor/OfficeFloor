/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.resource;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link ResourceLocator} for a file system.
 * 
 * @author Daniel Sagenschneider
 */
public class FileSystemResourceLocator implements ResourceLocator {

	/**
	 * Root of file system for which all paths are relative.
	 */
	private final File root;

	/**
	 * Initiate.
	 * 
	 * @param root
	 *            Root of file system for which all paths are relative.
	 */
	public FileSystemResourceLocator(File root) {
		this.root = root;
	}

	/*
	 * ====================== ResourceLocator ======================
	 */

	@Override
	public URL getResource(String resourcePath) throws MalformedURLException {
		// TODO implement ResourceLocator.getResource
		throw new UnsupportedOperationException(
				"TODO implement ResourceLocator.getResource");
	}

	@Override
	public InputStream getResourceAsStream(String resourcePath) {
		// TODO implement ResourceLocator.getResourceAsStream
		throw new UnsupportedOperationException(
				"TODO implement ResourceLocator.getResourceAsStream");
	}

	@Override
	public Set<String> getResourceChildren(String resourcePath) {

		// Obtain the directory
		File directory = new File(this.root, resourcePath);

		// Ensure is a directory
		if (!directory.isDirectory()) {
			// Not directory so no children
			return Collections.emptySet();
		}

		// Obtain listing of children
		return new HashSet<String>(Arrays.asList(directory.list()));
	}

}