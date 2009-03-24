/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * {@link HttpResponse} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel
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
	 * Obtains the {@link OutputStream} to write the body of the response.
	 * <p>
	 * The returned {@link OutputStream} works in conjunction with
	 * {@link #appendToBody(ByteBuffer)}.
	 * 
	 * @return {@link OutputStream} to write the body of the response.
	 */
	OutputStream getBody();

	/**
	 * <p>
	 * Appends the content to the body of the HTTP response.
	 * <p>
	 * This enables for optimisations for {@link java.nio.DirectByteBuffer}
	 * instances.
	 * 
	 * @param content
	 *            Content to appended to the HTTP response.
	 */
	void appendToBody(ByteBuffer content);

	/**
	 * Sends this HTTP response to the client.
	 * 
	 * @throws IOException
	 *             If fails to trigger sending the message.
	 */
	void send() throws IOException;

}