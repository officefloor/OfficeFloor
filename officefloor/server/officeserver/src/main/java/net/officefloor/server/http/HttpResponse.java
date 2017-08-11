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
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getHttpVersion();

	/**
	 * <p>
	 * Override the {@link HttpVersion}.
	 * <p>
	 * This defaults to value on {@link HttpRequest}.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 */
	void setHttpVersion(HttpVersion version);

	/**
	 * <p>
	 * Obtains the {@link HttpStatus}.
	 * <p>
	 * This is the current status. The status may changed based on particular
	 * HTTP rules (e.g. 200 becoming 204 due to no entity) or there being a
	 * failure in processing the message.
	 * 
	 * @return Current {@link HttpStatus}.
	 */
	HttpStatus getHttpStatus();

	/**
	 * <p>
	 * Specifies the {@link HttpStatus}.
	 * <p>
	 * This defaults to {@link HttpStatus#OK} assuming the request was processed
	 * successfully.
	 * 
	 * @param status
	 *            {@link HttpStatus}.
	 */
	void setHttpStatus(HttpStatus status);

	/**
	 * Obtains the {@link HttpResponseHeaders}.
	 * 
	 * @return {@link HttpResponseHeaders}.
	 */
	HttpResponseHeaders getHttpHeaders();

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
	 *            require character encoding (including the default HTTP content
	 *            encoding).
	 * @throws IOException
	 *             If attempting to specify after calling
	 *             {@link #getEntityWriter()}.
	 */
	void setContentType(String contentType, Charset charset) throws IOException;

	/**
	 * <p>
	 * Provides means to use {@link HttpHeaderValue} to specify both the
	 * <code>Content-Type</code> and <code>charset</code> for more efficiency.
	 * <p>
	 * Note that {@link HttpHeaderValue} will require the inclusion of the
	 * <code>charset</code>, as the <code>charset</code> will not be appended.
	 * 
	 * @param contentTypeAndCharsetValue
	 *            {@link HttpHeaderValue} for the <code>Content-Type</code> and
	 *            <code>charset</code>.
	 * @param charset
	 *            {@link Charset} to configure the {@link ServerWriter}.
	 * @throws IOException
	 *             If attempting to specify after calling
	 *             {@link #getEntityWriter()}.
	 */
	void setContentType(HttpHeaderValue contentTypeAndCharsetValue, Charset charset) throws IOException;

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
	 * Resets the {@link HttpResponse} by clearing {@link HttpHeader} instances
	 * and the entity.
	 * 
	 * @throws IOException
	 *             If committed to send the {@link HttpResponse}.
	 */
	void reset() throws IOException;

	/**
	 * Sends this {@link HttpResponse}. After calling this the
	 * {@link ServerOutputStream} or {@link ServerWriter} is closed.
	 * 
	 * @throws IOException
	 *             If fails to send this {@link HttpResponse}.
	 */
	void send() throws IOException;

}