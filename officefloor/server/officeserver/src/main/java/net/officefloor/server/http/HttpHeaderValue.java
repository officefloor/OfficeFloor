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
 * <p>
 * Provides formatting of values for {@link HttpHeader} values.
 * <p>
 * Also provides means for common {@link HttpHeader} values in already encoded
 * HTTP bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderValue {

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Pre-encoded bytes of value ready for HTTP output.
	 */
	private final byte[] encodedValue;

	/**
	 * Instantiate.
	 * 
	 * @param value {@link HttpHeaderValue}.
	 */
	public HttpHeaderValue(String value) {
		this.value = value;
		this.encodedValue = this.value.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Obtains the value.
	 * 
	 * @return value.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Writes this {@link HttpHeaderValue} to the {@link StreamBuffer}.
	 * 
	 * @param <B>        Buffer type.
	 * @param head       Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		StreamBuffer.write(this.encodedValue, head, bufferPool);
	}

}
