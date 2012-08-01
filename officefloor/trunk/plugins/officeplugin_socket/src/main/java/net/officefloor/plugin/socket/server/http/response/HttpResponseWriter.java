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

package net.officefloor.plugin.socket.server.http.response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * Writes content to the {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
// TODO may just use Writer wrapper around ByteOutputStream
@Deprecated
public interface HttpResponseWriter {

	/**
	 * Writes content to the {@link HttpResponse}.
	 *
	 * @param contentEncoding
	 *            <code>Content-Encoding</code> of the contents to write. May be
	 *            <code>null</code> if <code>Content-Encoding</code> is unknown.
	 * @param contentType
	 *            <code>Content-Type</code> of the contents to write. May be
	 *            <code>null</code> if <code>Content-Type</code> is unknown.
	 * @param charset
	 *            {@link Charset} of the contents to write. May be
	 *            <code>null</code> if the contents is not text or the
	 *            {@link Charset} is unknown.
	 * @param contents
	 *            Contents to write to the {@link HttpResponse}.
	 * @throws IOException
	 *             If fails to write contents to {@link HttpResponse}.
	 */
	void write(String contentEncoding, String contentType, Charset charset,
			ByteBuffer contents) throws IOException;

	/**
	 * <p>
	 * Writes content to the {@link HttpResponse}.
	 * <p>
	 * As the content is {@link String}, there should be no
	 * <code>Content-Encoding</code> on the contents to write.
	 *
	 * @param contentType
	 *            <code>Content-Type</code> of the contents to write. May be
	 *            <code>null</code> if <code>Content-Type</code> is unknown.
	 * @param contents
	 *            Contents to write to the {@link HttpResponse}.
	 * @throws IOException
	 *             If fails to write contents to {@link HttpResponse}.
	 */
	void write(String contentType, String contents) throws IOException;

}