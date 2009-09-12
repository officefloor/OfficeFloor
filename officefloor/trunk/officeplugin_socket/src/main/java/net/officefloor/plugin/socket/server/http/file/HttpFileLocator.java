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

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Locator to locate a {@link HttpFile}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpFileLocator {

	/**
	 * <p>
	 * Adds a {@link HttpFileDescriber} to describe the located {@link HttpFile}
	 * instances.
	 * <p>
	 * The {@link HttpFileDescriber} instances are to be used in the order
	 * added. Once a {@link HttpFileDescription} is provided, the remaining
	 * {@link HttpFileDescriber} instances will not be used.
	 *
	 * @param httpFileDescriber
	 *            {@link HttpFileDescriber} to describe the located
	 *            {@link HttpFile}.
	 */
	void addHttpFileDescriber(HttpFileDescriber httpFileDescriber);

	/**
	 * <p>
	 * Locates the {@link HttpFile}.
	 * <p>
	 * The path on the returned {@link HttpFile} may be different to the input
	 * path as it is transformed to a canonical path.
	 *
	 * @param requestUriPath
	 *            {@link HttpRequest} path to the {@link HttpFile}.
	 * @param {@link HttpFileDescriber} instances specific to this locate to use
	 *        before the added {@link HttpFileDescriber} instances.
	 * @return {@link HttpFile}.
	 * @throws IOException
	 *             If failure in finding the {@link HttpFile}.
	 * @throws InvalidHttpRequestUriException
	 *             Should the request URI be invalid.
	 */
	HttpFile locateHttpFile(String requestUriPath,
			HttpFileDescriber... httpFileDescribers) throws IOException,
			InvalidHttpRequestUriException;

}