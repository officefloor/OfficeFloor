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
 * Means to provide common {@link HttpHeader} names in already encoded HTTP
 * bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderName {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Pre-encoded bytes of name ready for HTTP output.
	 */
	private final byte[] encodedName;

	/**
	 * Instantiate.
	 * 
	 * @param name {@link HttpHeaderName}.
	 */
	public HttpHeaderName(String name) {
		this(name, false);
	}

	/**
	 * Instantiate.
	 * 
	 * @param name           {@link HttpHeaderName}.
	 * @param isMaintainCase Whether to maintain {@link HttpHeaderName} case.
	 */
	public HttpHeaderName(String name, boolean isMaintainCase) {
		this.name = isMaintainCase ? name : name.toLowerCase(); // case insensitive
		this.encodedName = this.name.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Writes this {@link HttpHeaderName} to the {@link StreamBuffer}.
	 * 
	 * @param <B>        Buffer type.
	 * @param head       Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		StreamBuffer.write(this.encodedName, head, bufferPool);
	}

}
