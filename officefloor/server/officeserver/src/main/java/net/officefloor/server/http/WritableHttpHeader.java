/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
