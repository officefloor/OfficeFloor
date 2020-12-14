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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.ServerMemoryOverloadedException;
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
	private static final byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * = encoded bytes.
	 */
	private static final byte[] EQUALS = "=".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <code>Expires</code> prefix.
	 */
	private static final String EXPIRES_STRING = "; Expires=";

	/**
	 * <code>Expires</code> prefix bytes.
	 */
	private static final byte[] EXPIRES_BYTES = EXPIRES_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * Expires {@link DateTimeFormatter}. Default {@link ZoneId} to allow
	 * {@link Instant#now()} (and derived {@link TemporalAccessor} instances) to be
	 * formatted.
	 */
	private static final DateTimeFormatter EXPIRE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME
			.withZone(ZoneId.of("GMT"));

	/**
	 * <code>Max-Age</code> prefix.
	 */
	private static final String MAX_AGE_STRING = "; Max-Age=";

	/**
	 * <code>Max-Age</code> prefix bytes.
	 */
	private static final byte[] MAX_AGE_BYTES = MAX_AGE_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <code>Domain</code> prefix.
	 */
	private static final String DOMAIN_STRING = "; Domain=";

	/**
	 * <code>Domain</code> prefix bytes.
	 */
	private static final byte[] DOMAIN_BYTES = DOMAIN_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <code>Path</code> prefix.
	 */
	private static final String PATH_STRING = "; Path=";

	/**
	 * <code>Path</code> prefix bytes.
	 */
	private static final byte[] PATH_BYTES = PATH_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <code>Secure<code>.
	 */
	private static final String SECURE_STRING = "; Secure";

	/**
	 * <code>Secure</code> bytes.
	 */
	private static final byte[] SECURE_BYTES = SECURE_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <code>HttpOnly</code>.
	 */
	private static final String HTTP_ONLY_STRING = "; HttpOnly";

	/**
	 * <code>HttpOnly</code> bytes.
	 */
	private static final byte[] HTTP_ONLY_BYTES = HTTP_ONLY_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * Extension attribute.
	 */
	private static final String EXTENSION_STRING = "; ";

	/**
	 * Extension attribute prefix.
	 */
	private static final byte[] EXTENSION_BYTES = EXTENSION_STRING.getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * {@link HttpHeader} end of line encoded bytes.
	 */
	private static final byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * No extensions.
	 */
	private static final String[] NO_EXTENSIONS = new String[0];

	/**
	 * Cached {@link StringBuilder} to reduce object creation, when creating
	 * {@link HttpHeader} value.
	 */
	private static final ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder(256);
		}
	};

	/**
	 * Next {@link WritableHttpCookie} to enable chaining together into linked list.
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
	 * {@link ManagedObjectContext}.
	 */
	private final ManagedObjectContext context;

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
	 * @param name    Name.
	 * @param value   Value.
	 * @param context {@link ManagedObjectContext}.
	 */
	public WritableHttpCookie(String name, String value, ManagedObjectContext context) {
		this.name = name;
		this.value = value;
		this.context = context;
	}

	/**
	 * Writes this HTTP Cookie to the {@link StreamBuffer} stream.
	 * 
	 * @param <B>                           Buffer type.
	 * @param head                          Head {@link StreamBuffer} of linked list
	 *                                      of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {
		SET_COOKIE.write(head, bufferPool, serverMemoryOverloadedHandler);
		StreamBuffer.write(COLON_SPACE, head, bufferPool, serverMemoryOverloadedHandler);
		StreamBuffer.write(this.name, head, bufferPool, serverMemoryOverloadedHandler);
		StreamBuffer.write(EQUALS, head, bufferPool, serverMemoryOverloadedHandler);
		StreamBuffer.write(this.value, head, bufferPool, serverMemoryOverloadedHandler);
		if (this.expires != null) {
			StreamBuffer.write(EXPIRES_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
			EXPIRE_FORMATTER.formatTo(this.expires,
					StreamBuffer.getAppendable(head, bufferPool, serverMemoryOverloadedHandler));
		}
		if (this.maxAge != BROWSER_SESSION_MAX_AGE) {
			StreamBuffer.write(MAX_AGE_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
			StreamBuffer.write(this.maxAge, head, bufferPool, serverMemoryOverloadedHandler);
		}
		if (this.domain != null) {
			StreamBuffer.write(DOMAIN_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
			StreamBuffer.write(this.domain, head, bufferPool, serverMemoryOverloadedHandler);
		}
		if (this.path != null) {
			StreamBuffer.write(PATH_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
			StreamBuffer.write(this.path, head, bufferPool, serverMemoryOverloadedHandler);
		}
		if (this.isSecure) {
			StreamBuffer.write(SECURE_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
		}
		if (this.isHttpOnly) {
			StreamBuffer.write(HTTP_ONLY_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
		}
		if (this.extensions != null) {
			for (String extension : this.extensions) {
				StreamBuffer.write(EXTENSION_BYTES, head, bufferPool, serverMemoryOverloadedHandler);
				StreamBuffer.write(extension, head, bufferPool, serverMemoryOverloadedHandler);
			}
		}
		StreamBuffer.write(HEADER_EOLN, head, bufferPool, serverMemoryOverloadedHandler);
	}

	/**
	 * Obtains the HTTP Cookie value for the HTTP response.
	 * 
	 * @return HTTP header value.
	 */
	public String toResponseHeaderValue() {
		StringBuilder value = stringBuilder.get();
		value.setLength(0); // clear
		value.append(this.name);
		value.append("=");
		value.append(this.value);
		if (this.expires != null) {
			value.append(EXPIRES_STRING);
			EXPIRE_FORMATTER.formatTo(this.expires, value);
		}
		if (this.maxAge != BROWSER_SESSION_MAX_AGE) {
			value.append(MAX_AGE_STRING);
			value.append(this.maxAge);
		}
		if (this.domain != null) {
			value.append(DOMAIN_STRING);
			value.append(this.domain);
		}
		if (this.path != null) {
			value.append(PATH_STRING);
			value.append(this.path);
		}
		if (this.isSecure) {
			value.append(SECURE_STRING);
		}
		if (this.isHttpOnly) {
			value.append(HTTP_ONLY_STRING);
		}
		if (this.extensions != null) {
			for (String extension : this.extensions) {
				value.append(EXTENSION_STRING);
				value.append(extension);
			}
		}
		return value.toString();
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
	public HttpResponseCookie setValue(String value) {
		return this.context.run(() -> {
			this.value = value;
			return this;
		});
	}

	@Override
	public TemporalAccessor getExpires() {
		return this.context.run(() -> this.expires);
	}

	@Override
	public HttpResponseCookie setExpires(TemporalAccessor expires) {
		return this.context.run(() -> {
			this.expires = expires;
			return this;
		});
	}

	@Override
	public long getMaxAge() {
		return this.context.run(() -> this.maxAge);
	}

	@Override
	public HttpResponseCookie setMaxAge(long maxAge) {
		return this.context.run(() -> {
			this.maxAge = maxAge;
			return this;
		});
	}

	@Override
	public String getDomain() {
		return this.context.run(() -> this.domain);
	}

	@Override
	public HttpResponseCookie setDomain(String domain) {
		return this.context.run(() -> {
			this.domain = domain;
			return this;
		});
	}

	@Override
	public String getPath() {
		return this.context.run(() -> this.path);
	}

	@Override
	public HttpResponseCookie setPath(String path) {
		return this.context.run(() -> {
			this.path = path;
			return this;
		});
	}

	@Override
	public boolean isSecure() {
		return this.context.run(() -> this.isSecure);
	}

	@Override
	public HttpResponseCookie setSecure(boolean isSecure) {
		return this.context.run(() -> {
			this.isSecure = isSecure;
			return this;
		});
	}

	@Override
	public boolean isHttpOnly() {
		return this.context.run(() -> this.isHttpOnly);
	}

	@Override
	public HttpResponseCookie setHttpOnly(boolean isHttpOnly) {
		return this.context.run(() -> {
			this.isHttpOnly = isHttpOnly;
			return this;
		});
	}

	@Override
	public HttpResponseCookie addExtension(String extension) {
		return this.context.run(() -> {

			// Lazy create the extension list
			if (this.extensions == null) {
				this.extensions = new ArrayList<>(1);
			}

			// Add the extension
			this.extensions.add(extension);

			// Return
			return this;
		});
	}

	@Override
	public String[] getExtensions() {
		return this.context.run(() -> this.extensions == null ? NO_EXTENSIONS
				: this.extensions.toArray(new String[this.extensions.size()]));
	}

	@Override
	public HttpResponseCookie clearAttributes() {
		return this.context.run(() -> {
			this.expires = null;
			this.maxAge = BROWSER_SESSION_MAX_AGE;
			this.domain = null;
			this.path = null;
			this.isSecure = false;
			this.isHttpOnly = false;
			this.extensions = null;
			return this;
		});
	}

	@Override
	public HttpResponseCookie configure(Consumer<HttpResponseCookie> configurer) {
		return this.context.run(() -> {
			configurer.accept(this);
			return this;
		});
	}

}
