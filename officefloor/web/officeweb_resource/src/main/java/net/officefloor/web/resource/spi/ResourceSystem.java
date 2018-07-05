/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.file.Path;

import net.officefloor.web.resource.HttpResourceStore;

/**
 * Underlying system to the {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystem {

	/**
	 * Obtains the {@link Path} to the resource.
	 * 
	 * @param path
	 *            Path for the resource.
	 * @return {@link Path} if resource found, otherwise <code>null</code>.
	 * @throws IOException
	 *             If failure in obtaining {@link Path}.
	 */
	Path getResource(String path) throws IOException;

}