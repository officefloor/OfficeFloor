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
package net.officefloor.web.resource.spi;

import java.nio.file.Path;
import java.io.Closeable;
import java.io.IOException;

/**
 * Cache of files.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCache extends Closeable {

	/**
	 * Creates a new file.
	 * 
	 * @param name
	 *            Name to aid in identifying the file for debugging.
	 * @return {@link Path} to the new file.
	 * @throws IOException
	 *             If fails to create the file.
	 */
	Path createFile(String name) throws IOException;

	/**
	 * Creates a new directory.
	 *
	 * @param name
	 *            Name to aid in identifying the file for debugging.
	 * @return {@link Path} to the new directory.
	 * @throws IOException
	 *             If fails to create the directory.
	 */
	Path createDirectory(String name) throws IOException;

}