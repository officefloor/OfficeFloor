/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.parse;

import java.io.IOException;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * Parses a {@link HttpRequest}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpRequestParser {

	/**
	 * Parses the {@link HttpRequest} bytes from the {@link InputBufferStream}.
	 *
	 * @param inputBufferStream
	 *            {@link InputBufferStream} containing the data to be parsed for
	 *            the {@link HttpRequest}.
	 * @param tempBuffer
	 *            Temporary char buffer that is used to translate bytes into a
	 *            {@link String}. This allows reducing memory size and creation
	 *            of arrays. The size of the buffer indicates the maximum size
	 *            for parts of the {@link HttpRequest} (except the body).
	 * @return <code>true</code> if the {@link HttpRequest} has been fully
	 *         parsed. <false>false</false> indicates this method should be
	 *         called again when further data is available on the
	 *         {@link InputBufferStream} to obtain the full {@link HttpRequest}.
	 * @throws IOException
	 *             If fails to read bytes.
	 * @throws HttpRequestParseException
	 *             If failure to parse {@link HttpRequest}.
	 */
	boolean parse(InputBufferStream inputBufferStream, char[] tempBuffer)
			throws IOException, HttpRequestParseException;

	/**
	 * Resets for parsing another {@link HttpRequest}.
	 */
	void reset();

	/**
	 * Obtains the method.
	 *
	 * @return Method.
	 */
	String getMethod();

	/**
	 * Obtains the request URI.
	 *
	 * @return Request URI.
	 */
	String getRequestURI();

	/**
	 * Obtains the HTTP version.
	 *
	 * @return HTTP version.
	 */
	String getHttpVersion();

	/**
	 * Obtains the {@link HttpHeader} instances in the order supplied.
	 *
	 * @return {@link HttpHeader} instances in the order supplied.
	 */
	List<HttpHeader> getHeaders();

	/**
	 * Obtains the {@link InputBufferStream} to the body of the
	 * {@link HttpRequest}.
	 *
	 * @return {@link InputBufferStream} to the body of the {@link HttpRequest}.
	 */
	InputBufferStream getBody();

}