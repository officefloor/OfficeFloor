/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Writable HTTP Cookie.
 * 
 * @author Daniel Sagenschneider
 */
public class WritableHttpCookie implements HttpResponseCookie {

	/**
	 * <code>Set-Cookie</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName SET_COOKIE = new HttpHeaderName("set-cookie");

	/**
	 * : then space encoded bytes.
	 */
	private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * = encoded bytes.
	 */
	private static byte[] EQUALS = "=".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeader} end of line encoded bytes.
	 */
	private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * Next {@link WritableHttpCookie} to enable chaining together into linked
	 * list.
	 */
	public WritableHttpCookie next = null;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private String value;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

	/**
	 * Expires.
	 */
	private TemporalAccessor expires = null;

	/**
	 * Max age.
	 */
	private long maxAge = BROWSER_SESSION_MAX_AGE;

	/**
	 * Domain.
	 */
	private String domain = null;

	/**
	 * Path.
	 */
	private String path = null;

	/**
	 * Indicates if secure.
	 */
	private boolean isSecure = false;

	/**
	 * Indicates if HTTP only.
	 */
	private boolean isHttpOnly = false;

	/**
	 * Extensions.
	 */
	private List<String> extensions = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public WritableHttpCookie(String name, String value, ProcessAwareContext context) {
		this.name = name;
		this.value = value;
		this.context = context;
	}

	/**
	 * Writes this HTTP Cookie to the {@link StreamBuffer}.
	 * 
	 * @param head
	 *            Head {@link StreamBuffer} of linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		SET_COOKIE.write(head, bufferPool);
		StreamBuffer.write(COLON_SPACE, 0, COLON_SPACE.length, head, bufferPool);
		StreamBuffer.write(this.name, head, bufferPool);
		StreamBuffer.write(EQUALS, 0, EQUALS.length, head, bufferPool);
		StreamBuffer.write(this.value, head, bufferPool);
		StreamBuffer.write(HEADER_EOLN, 0, HEADER_EOLN.length, head, bufferPool);
	}

	/*
	 * ==================== HttpResponseCookie =====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.context.run(() -> this.value);
	}

	@Override
	public void setValue(String value) {
		this.context.run(() -> this.value = value);
	}

	@Override
	public TemporalAccessor getExpires() {
		return this.context.run(() -> this.expires);
	}

	@Override
	public void setExpires(TemporalAccessor expires) {
		this.context.run(() -> this.expires = expires);
	}

	@Override
	public long getMaxAge() {
		return this.context.run(() -> this.maxAge);
	}

	@Override
	public void setMaxAge(long maxAge) {
		this.context.run(() -> this.maxAge = maxAge);
	}

	@Override
	public String getDomain() {
		return this.context.run(() -> this.domain);
	}

	@Override
	public void setDomain(String domain) {
		this.context.run(() -> this.domain = domain);
	}

	@Override
	public String getPath() {
		return this.context.run(() -> this.path);
	}

	@Override
	public void setPath(String path) {
		this.context.run(() -> this.path = path);
	}

	@Override
	public boolean isSecure() {
		return this.context.run(() -> this.isSecure);
	}

	@Override
	public void setSecure(boolean isSecure) {
		this.context.run(() -> this.isSecure = isSecure);
	}

	@Override
	public boolean isHttpOnly() {
		return this.context.run(() -> this.isHttpOnly);
	}

	@Override
	public void setHttpOnly(boolean isHttpOnly) {
		this.context.run(() -> this.isHttpOnly = isHttpOnly);
	}

	@Override
	public void addExtension(String extension) {
		this.context.run(() -> {

			// Lazy create the extension list
			if (this.extensions == null) {
				this.extensions = new ArrayList<>(1);
			}

			// Add the extension
			this.extensions.add(extension);

			// Void return
			return null;
		});
	}

	@Override
	public String[] getExtensions() {
		return this.context.run(() -> this.extensions.toArray(new String[this.extensions.size()]));
	}

}