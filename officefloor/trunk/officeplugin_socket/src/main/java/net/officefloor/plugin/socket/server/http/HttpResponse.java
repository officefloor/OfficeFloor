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
package net.officefloor.plugin.socket.server.http;

import java.io.IOException;

import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link HttpResponse} for the {@link ServerHttpConnection}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpResponse {

	/**
	 * <p>
	 * Allows specifying the HTTP version on the response. Values should be
	 * either HTTP/1.0 or HTTP/1.1.
	 * <p>
	 * This defaults to value on {@link HttpRequest}.
	 *
	 * @param version
	 *            HTTP version.
	 */
	void setVersion(String version);

	/**
	 * <p>
	 * Specifies the status of the response with the default status message.
	 * <p>
	 * This defaults to 200 assuming the request was processed successfully.
	 *
	 * @param status
	 *            Status of the response.
	 */
	void setStatus(int status);

	/**
	 * Specifies the status of the response including specifying the status
	 * human readable message.
	 *
	 * @param status
	 *            Status of the response.
	 * @param statusMessage
	 *            Human readable status message.
	 * @see #setStatus(int)
	 */
	void setStatus(int status, String statusMessage);

	/**
	 * <p>
	 * Specifies a header on the response.
	 * <p>
	 * Headers are provided on the response in the order they are added.
	 *
	 * @param name
	 *            Name of header.
	 * @param value
	 *            Value of header.
	 */
	void addHeader(String name, String value);

	/**
	 * <p>
	 * Obtains the {@link OutputBufferStream} to write the body of the response.
	 * <p>
	 * Closing the returned {@link OutputBufferStream} is similar to calling
	 * {@link #send()}.
	 *
	 * @return {@link OutputBufferStream} to write the body of the response.
	 *
	 * @see #send()
	 */
	OutputBufferStream getBody();

	/**
	 * Sends this {@link HttpResponse}. After calling this {@link #getBody()} is
	 * closed and the corresponding {@link HttpRequest#getBody()} is closed.
	 *
	 * @throws IOException
	 *             If fails to send this {@link HttpResponse}.
	 */
	void send() throws IOException;

}