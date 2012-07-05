/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.resource;

import java.io.IOException;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;

/**
 * Factory to create a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceFactory {

	/**
	 * <p>
	 * Adds a {@link HttpFileDescriber} to describe the {@link HttpFile}
	 * instances.
	 * <p>
	 * The {@link HttpFileDescriber} instances are to be used in the order
	 * added. Once a {@link HttpFileDescription} is provided, the remaining
	 * {@link HttpFileDescriber} instances will not be used.
	 * 
	 * @param httpFileDescriber
	 *            {@link HttpFileDescriber} to describe the {@link HttpFile}.
	 */
	void addHttpFileDescriber(HttpFileDescriber httpFileDescriber);

	/**
	 * <p>
	 * Creates the {@link HttpResponse}.
	 * <p>
	 * The path on the returned {@link HttpResource} may be different to the
	 * input path as it is transformed to a canonical path.
	 * 
	 * @param requestUriPath
	 *            {@link HttpRequest} path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException
	 *             If failure in finding the {@link HttpResource}.
	 * @throws InvalidHttpRequestUriException
	 *             Should the request URI be invalid.
	 */
	HttpResource createHttpResource(String requestUriPath) throws IOException,
			InvalidHttpRequestUriException;

}