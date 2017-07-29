/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

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
	 * Obtains the HTTP version.
	 * 
	 * @return HTTP version.
	 */
	String getVersion();

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
	 * <p>
	 * Obtains the status.
	 * <p>
	 * This is the current status. The status may changed based on particular
	 * HTTP rules (e.g. 200 becoming 204 due to no entity) or there being a
	 * failure in processing the message.
	 * 
	 * @return Current status.
	 */
	int getStatus();

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
	 * Obtains the current status message.
	 * 
	 * @return Current status message.
	 * 
	 * @see #getStatus()
	 */
	String getStatusMessage();

	/**
	 * Resets the {@link HttpResponse} by clearing {@link HttpHeader} instances
	 * and the entity.
	 * 
	 * @throws IOException
	 *             If committed to send the {@link HttpResponse}.
	 */
	void reset() throws IOException;

	/**
	 * <p>
	 * Adds a {@link HttpHeader} for the response.
	 * <p>
	 * {@link HttpHeader} instances are provided on the response in the order
	 * they are added.
	 * 
	 * @param name
	 *            Name of {@link HttpHeader}.
	 * @param value
	 *            Value of {@link HttpHeader}.
	 * @return {@link HttpHeader} instance added.
	 * @throws IllegalArgumentException
	 *             Should the {@link HttpHeader} be managed by the
	 *             {@link HttpResponse}.
	 */
	HttpHeader addHeader(String name, String value)
			throws IllegalArgumentException;

	/**
	 * Obtains the first {@link HttpHeader} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader}.
	 * @return First {@link HttpHeader} by the name.
	 */
	HttpHeader getHeader(String name);

	/**
	 * Obtains all the {@link HttpHeader} instances for the response.
	 * 
	 * @return All the {@link HttpHeader} instances for the response.
	 */
	HttpHeader[] getHeaders();

	/**
	 * Removes the particular {@link HttpHeader} from the response.
	 * 
	 * @param header
	 *            {@link HttpHeader} to be removed from the response.
	 */
	void removeHeader(HttpHeader header);

	/**
	 * <p>
	 * Removes all {@link HttpHeader} instances by the name.
	 * <p>
	 * This method compliments {@link #addHeader(String, String)} to allow
	 * adding a new single {@link HttpHeader} instance by name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader} instances to remove.
	 */
	void removeHeaders(String name);

	/**
	 * <p>
	 * Specifies the <code>Content-Type</code> and optionally the
	 * {@link Charset}. The <code>charset</code> parameter will automatically be
	 * added to the <code>Content-Type</code> on using the
	 * {@link #getEntityWriter}.
	 * <p>
	 * This must be specified before calling {@link #getEntityWriter()}.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code>. May be <code>null</code> to unset
	 *            the <code>Content-Type</code>.
	 * @param charset
	 *            {@link Charset} for the <code>Content-Type</code>. This may be
	 *            <code>null</code> to use the default {@link Charset}. Also use
	 *            <code>null</code> for <code>Content-Type</code>s that do not
	 *            require character encoding.
	 * @throws IOException
	 *             If attempting to specify after calling
	 *             {@link #getEntityWriter()}.
	 */
	void setContentType(String contentType, Charset charset) throws IOException;

	/**
	 * Obtains the <code>Content-Type</code>.
	 * 
	 * @return <code>Content-Type</code>. May be <code>null</code> if no
	 *         <code>Content-Type</code> has been specified.
	 */
	String getContentType();

	/**
	 * <p>
	 * Obtains the {@link Charset} for the content.
	 * <p>
	 * If no {@link Charset} has been specified, the default {@link Charset}
	 * will be returned.
	 * 
	 * @return {@link Charset}.
	 */
	Charset getContentCharset();

	/**
	 * <p>
	 * Obtains the {@link ServerOutputStream} to write the entity of the
	 * response.
	 * <p>
	 * Only one of {@link #getEntity()} or {@link #getEntityWriter()} may be
	 * called.
	 * <p>
	 * Closing the returned {@link ServerOutputStream} is similar to calling
	 * {@link #send()}.
	 * 
	 * @return {@link ServerOutputStream} to write the entity of the response.
	 * @throws IOException
	 *             Should {@link #getEntityWriter()} already be provided.
	 * 
	 * @see #getEntityWriter()
	 * @see #send()
	 */
	ServerOutputStream getEntity() throws IOException;

	/**
	 * <p>
	 * Obtains the {@link ServerWriter} to write the entity of the response.
	 * <p>
	 * Only one of {@link #getEntity()} or {@link #getEntityWriter()} may be
	 * called.
	 * <p>
	 * Closing the returned {@link ServerOutputStream} is similar to calling
	 * {@link #send()}.
	 * 
	 * @return {@link ServerWriter} to write the entity of the response.
	 * @throws IOException
	 *             Should {@link #getEntity()} already be provided.
	 * 
	 * @see #getEntity()
	 * @see #send()
	 */
	ServerWriter getEntityWriter() throws IOException;

	/**
	 * Sends this {@link HttpResponse}. After calling this the
	 * {@link ServerOutputStream} or {@link ServerWriter} is closed.
	 * 
	 * @throws IOException
	 *             If fails to send this {@link HttpResponse}.
	 */
	void send() throws IOException;

}