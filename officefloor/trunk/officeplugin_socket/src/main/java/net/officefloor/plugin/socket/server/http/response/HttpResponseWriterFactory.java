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
package net.officefloor.plugin.socket.server.http.response;

import java.io.IOException;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Factory for the creation of a {@link HttpResponseWriter}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpResponseWriterFactory {

	/**
	 * Creates the {@link HttpResponseWriter} for the
	 * {@link ServerHttpConnection}.
	 *
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return {@link HttpResponseWriter} for the {@link ServerHttpConnection}.
	 * @throws IOException
	 *             If unable to provide {@link HttpResponseWriter} to write
	 *             content similar to content already on the
	 *             {@link HttpResponse}.
	 */
	HttpResponseWriter createHttpResponseWriter(ServerHttpConnection connection)
			throws IOException;

}