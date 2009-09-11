/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file;

import java.io.IOException;

/**
 * Locator to locate a {@link HttpFile}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpFileLocator {

	/**
	 * <p>
	 * Locates the {@link HttpFile}.
	 * <p>
	 * The path on the returned {@link HttpFile} may be different to the input
	 * path as it is transformed to a canonical path.
	 *
	 * @param path
	 *            Path to the {@link HttpFile}.
	 * @return {@link HttpFile} or <code>null</code> if not found.
	 * @throws IOException
	 *             If fails to find the {@link HttpFile}.
	 */
	HttpFile locateHttpFile(String path) throws IOException;

}