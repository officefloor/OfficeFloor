/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Writable {@link HttpHeader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WritableHttpHeader implements HttpHeader {

	/**
	 * : then space encoded bytes.
	 */
	private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeader} end of line encoded bytes.
	 */
	private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeaderName}.
	 */
	private final HttpHeaderName name;

	/**
	 * {@link HttpHeaderValue}.
	 */
	private final HttpHeaderValue value;

	/**
	 * Next {@link WritableHttpHeader} to enable chaining together into linked list.
	 */
	public WritableHttpHeader next = null;

	/**
	 * Instantiate.
	 * 
	 * @param name  {@link HttpHeaderName}.
	 * @param value {@link HttpHeaderValue}.
	 */
	public WritableHttpHeader(HttpHeaderName name, HttpHeaderValue value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Instantiate.
	 * 
	 * @param name  {@link HttpHeaderName}.
	 * @param value {@link HttpHeaderValue}.
	 */
	public WritableHttpHeader(HttpHeaderName name, String value) {
		this(name, new HttpHeaderValue(value));
	}

	/**
	 * Instantiate.
	 * 
	 * @param name  {@link HttpHeaderName}.
	 * @param value {@link HttpHeaderValue}.
	 */
	public WritableHttpHeader(String name, HttpHeaderValue value) {
		this(new HttpHeaderName(name), value);
	}

	/**
	 * Instantiate.
	 * 
	 * @param name  {@link HttpHeaderName}.
	 * @param value {@link HttpHeaderValue}.
	 */
	public WritableHttpHeader(String name, String value) {
		this(new HttpHeaderName(name), new HttpHeaderValue(value));
	}

	/**
	 * Writes this {@link HttpHeader} to the {@link StreamBuffer}.
	 * 
	 * @param <B>        Buffer type.
	 * @param head       Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		this.name.write(head, bufferPool);
		StreamBuffer.write(COLON_SPACE, head, bufferPool);
		this.value.write(head, bufferPool);
		StreamBuffer.write(HEADER_EOLN, head, bufferPool);
	}

	/*
	 * ==================== HttpHeader ====================
	 */

	@Override
	public String getName() {
		return this.name.getName();
	}

	@Override
	public String getValue() {
		return this.value.getValue();
	}

}
