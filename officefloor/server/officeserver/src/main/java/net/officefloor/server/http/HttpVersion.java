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

import java.io.Serializable;

import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.ServerMemoryOverloadedException;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * HTTP version.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpVersion implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	// HTTP version names
	private static final String HTTP_1_0_NAME = "HTTP/1.0";
	private static final String HTTP_1_1_NAME = "HTTP/1.1";

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @param version {@link HttpVersion}.
	 * @return {@link HttpVersion}.
	 */
	public static HttpVersion getHttpVersion(String version) {
		switch (version) {
		case HTTP_1_0_NAME:
			return HTTP_1_0;
		case HTTP_1_1_NAME:
			return HTTP_1_1;
		default:
			return new HttpVersion(version);
		}
	}

	/**
	 * {@link HttpVersion} {@link Enum} for common HTTP methods.
	 */
	public enum HttpVersionEnum {

		/**
		 * Common {@link HttpVersion} instances.
		 */
		HTTP_1_0(HTTP_1_0_NAME), HTTP_1_1(HTTP_1_1_NAME),

		/**
		 * Non common {@link HttpVersion}.
		 */
		OTHER(null);

		/**
		 * {@link HttpVersion}.
		 */
		private final HttpVersion httpVersion;

		/**
		 * Instantiate.
		 * 
		 * @param httpVersionName Name of the {@link HttpVersion}.
		 */
		private HttpVersionEnum(String httpVersionName) {
			this.httpVersion = new HttpVersion(httpVersionName, this);
		}

		/**
		 * <p>
		 * Obtains the singleton {@link HttpVersion} for this {@link HttpVersionEnum}.
		 * <p>
		 * Note for {@link HttpVersionEnum#OTHER} this returns <code>null</code>.
		 * 
		 * @return {@link HttpVersion}.
		 */
		public HttpVersion getHttpVersion() {
			return this.httpVersion;
		}
	}

	/**
	 * {@link HttpVersion} 1.0.
	 */
	public static HttpVersion HTTP_1_0 = HttpVersionEnum.HTTP_1_0.getHttpVersion();

	/**
	 * {@link HttpVersion} 1.1.
	 */
	public static HttpVersion HTTP_1_1 = HttpVersionEnum.HTTP_1_1.getHttpVersion();

	/**
	 * Name of the {@link HttpVersion}.
	 */
	private final String name;

	/**
	 * HTTP encoded content for this {@link HttpVersion}.
	 */
	private final byte[] byteContent;

	/**
	 * Hash code.
	 */
	private final int hashCode;

	/**
	 * {@link HttpVersionEnum}.
	 */
	private final HttpVersionEnum httpVersionEnum;

	/**
	 * Instantiate a dynamic {@link HttpVersion}.
	 * 
	 * @param name Name of the {@link HttpVersion}.
	 */
	public HttpVersion(String name) {
		this(name, HttpVersionEnum.OTHER);
	}

	/**
	 * Instantiate.
	 * 
	 * @param name           Name of the {@link HttpVersion}.
	 * @param httpMethodEnum {@link HttpVersionEnum}.
	 */
	private HttpVersion(String name, HttpVersionEnum httpMethodEnum) {
		this.name = name;
		if (name == null) {
			this.byteContent = new byte[0];
			this.hashCode = "".hashCode();
		} else {
			this.byteContent = name.getBytes(ServerHttpConnection.HTTP_CHARSET);
			this.hashCode = name.hashCode();
		}
		this.httpVersionEnum = httpMethodEnum;
	}

	/**
	 * Equals without the type checking.
	 * 
	 * @param httpMethod {@link HttpVersion}.
	 * @return <code>true</code> if same {@link HttpVersion}.
	 */
	public boolean isEqual(HttpVersion httpMethod) {

		// Determine if match on enum
		if ((this.httpVersionEnum != HttpVersionEnum.OTHER) && (this.httpVersionEnum == httpMethod.httpVersionEnum)) {
			return true;
		}

		// Match based on name
		return this.name.equals(httpMethod.name);
	}

	/**
	 * Obtains the {@link HttpVersionEnum} for this {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersionEnum}.
	 */
	public HttpVersionEnum getEnum() {
		return this.httpVersionEnum;
	}

	/**
	 * Obtains the {@link HttpVersion} name.
	 * 
	 * @return {@link HttpVersion} name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Writes this {@link HttpStatus} to the {@link StreamBuffer}.
	 * 
	 * @param <B>                           Buffer type.
	 * @param head                          Head {@link StreamBuffer} of the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {
		StreamBuffer.write(this.byteContent, head, bufferPool, serverMemoryOverloadedHandler);
	}

	/*
	 * ================= Object ===============
	 */

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {

		// Determine if appropriate type
		if (!(obj instanceof HttpVersion)) {
			return false;
		}
		HttpVersion that = (HttpVersion) obj;

		// Determine if equal
		return this.isEqual(that);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
