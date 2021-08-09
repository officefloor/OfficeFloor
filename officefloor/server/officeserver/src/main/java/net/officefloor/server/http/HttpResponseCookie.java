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

import java.time.temporal.TemporalAccessor;
import java.util.function.Consumer;

/**
 * <p>
 * Cookie to send in the {@link HttpResponse}.
 * <p>
 * Cookie follows <a href="https://tools.ietf.org/html/rfc6265">RFC 6265</a>
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResponseCookie {

	/**
	 * Value of <code>Max-Age</code> indicating no age expire and should expire with
	 * the browser session.
	 */
	public static final long BROWSER_SESSION_MAX_AGE = -1;

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	String getValue();

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            Value.
	 * @return this.
	 */
	HttpResponseCookie setValue(String value);

	/**
	 * Obtains the expire time.
	 * 
	 * @return Expire time. May be <code>null</code> if no expire.
	 */
	TemporalAccessor getExpires();

	/**
	 * Sets the expire time.
	 * 
	 * @param expires
	 *            Expires time.
	 * @return this.
	 */
	HttpResponseCookie setExpires(TemporalAccessor expires);

	/**
	 * Obtains the maximum age in seconds.
	 * 
	 * @return Maximum age in seconds. Will be {@link #BROWSER_SESSION_MAX_AGE} if
	 *         not specified.
	 */
	long getMaxAge();

	/**
	 * <p>
	 * Sets the maximum age in seconds.
	 * <p>
	 * As per <a href="https://tools.ietf.org/html/rfc6265">RFC 6265</a> this
	 * overrides <code>Expires</code>.
	 * 
	 * @param maxAge
	 *            Maximum age in seconds.
	 * @return this.
	 */
	HttpResponseCookie setMaxAge(long maxAge);

	/**
	 * Obtains the domain.
	 * 
	 * @return Domain. May be <code>null</code>.
	 */
	String getDomain();

	/**
	 * Specifies the domain.
	 * 
	 * @param domain
	 *            Domain.
	 * @return this.
	 */
	HttpResponseCookie setDomain(String domain);

	/**
	 * Obtains the path.
	 * 
	 * @return Path. May be <code>null</code>.
	 */
	String getPath();

	/**
	 * Specifies the path.
	 * 
	 * @param path
	 *            Path.
	 * @return this.
	 */
	HttpResponseCookie setPath(String path);

	/**
	 * <p>
	 * Indicates if only communicated across secure a secure connection.
	 * <p>
	 * Note that, as per <a href="https://tools.ietf.org/html/rfc6265">RFC 6265</a>,
	 * this does not guarantee the security of the cookie contents. Cookies, as per
	 * the specification, are inherently insecure (such as any information sent to
	 * the client).
	 * 
	 * @return <code>true</code> to only communicate this Cookie across a secure
	 *         connection.
	 */
	boolean isSecure();

	/**
	 * Flags whether the client is only to send the Cookie over a secure connection.
	 * 
	 * @param isSecure
	 *            <code>true</code> to request the client to only send this Cookie
	 *            over a secure connection (assuming the client supports this).
	 * @return this.
	 */
	HttpResponseCookie setSecure(boolean isSecure);

	/**
	 * Indicates if the Cookie is only sent over HTTP connection.
	 * 
	 * @return <code>true</code> to only make this Cookie available in HTTP
	 *         requests.
	 */
	boolean isHttpOnly();

	/**
	 * Indicates if only available over HTTP requests (and not, for example, made
	 * available to JavaScript in the browser).
	 * 
	 * @param isHttpOnly
	 *            <code>true</code> to request the client to only send this Cookie
	 *            in HTTP requests, and not, for example, make available to
	 *            JavaScript in the browser.
	 * @return this.
	 */
	HttpResponseCookie setHttpOnly(boolean isHttpOnly);

	/**
	 * <p>
	 * Allows adding an extension.
	 * <p>
	 * The extensions are added as provided (separated by ';') to the end of the
	 * <code>Set-Cookie</code> {@link HttpHeader}.
	 * 
	 * @param extension
	 *            Extension.
	 * @return this.
	 */
	HttpResponseCookie addExtension(String extension);

	/**
	 * Obtains the extensions.
	 * 
	 * @return Extensions.
	 */
	String[] getExtensions();

	/**
	 * Clears the attributes.
	 * 
	 * @return this.
	 */
	HttpResponseCookie clearAttributes();

	/**
	 * Enables configuring multiple attributes with reduced locking.
	 * 
	 * @param configurer
	 *            {@link Consumer} to configured the {@link HttpResponseCookie}.
	 * @return this.
	 */
	HttpResponseCookie configure(Consumer<HttpResponseCookie> configurer);

}
