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

import java.io.IOException;

/**
 * Factory for the creation of a new {@link FileCache}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FileCacheFactory {

	/**
	 * Creates a new {@link FileCache}.
	 * 
	 * @param name
	 *            Name for the {@link FileCache}.
	 * @return New {@link FileCache}.
	 * @throws IOException
	 *             If fails to create a new {@link FileCache}.
	 */
	FileCache createFileCache(String name) throws IOException;

}