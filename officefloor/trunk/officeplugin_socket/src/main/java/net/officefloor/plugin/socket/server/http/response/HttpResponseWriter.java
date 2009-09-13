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
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Writes content to the {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpResponseWriter {

	/**
	 * Writes content to the {@link HttpResponse}.
	 *
	 * @param connection
	 *            {@link ServerHttpConnection}. This is provided as the
	 *            {@link HttpRequest} may need to be interrogated to determine
	 *            <code>acceptable</code> encoding and type of contents for the
	 *            client.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code> of the contents to write. May be
	 *            <code>null</code> if <code>Content-Encoding</code> is unknown.
	 * @param contentType
	 *            <code>Content-Type</code> of the contents to write. May be
	 *            <code>null</code> if <code>Content-Type</code> is unknown.
	 * @param contents
	 *            Contents to write to the {@link HttpResponse}.
	 * @throws IOException
	 *             If fails to write contents to {@link HttpResponse}.
	 */
	void writeContent(ServerHttpConnection connection, String contentEncoding,
			String contentType, ByteBuffer contents) throws IOException;

}