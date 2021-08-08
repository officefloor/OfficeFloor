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

import java.util.function.Consumer;

/**
 * {@link HttpResponseCookie} instances for the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResponseCookies extends Iterable<HttpResponseCookie> {

	/**
	 * <p>
	 * Sets a {@link HttpResponseCookie}.
	 * <p>
	 * If a {@link HttpResponseCookie} already exists by the name, it is updated to
	 * the value and returned. Otherwise a new {@link HttpResponseCookie} is added
	 * for the name.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @return {@link HttpRequestCookie}.
	 */
	HttpResponseCookie setCookie(String name, String value);

	/**
	 * <p>
	 * Sets a {@link HttpResponseCookie}.
	 * <p>
	 * If a {@link HttpResponseCookie} already exists by the name, it is updated to
	 * the value and returned. Otherwise a new {@link HttpResponseCookie} is added
	 * for the name.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param initialiser
	 *            Enables initialising the attributes of the
	 *            {@link HttpResponseCookie}. This reduces locking in setting
	 *            multiple attributes.
	 * @return {@link HttpResponseCookie}.
	 */
	HttpResponseCookie setCookie(String name, String value, Consumer<HttpResponseCookie> initialiser);

	/**
	 * <p>
	 * Sets a {@link HttpResponseCookie} from a {@link HttpRequestCookie}.
	 * <p>
	 * If a {@link HttpResponseCookie} already exists by the name, it is updated to
	 * the value and returned. Otherwise a new {@link HttpResponseCookie} is added
	 * for the name.
	 * <p>
	 * This is typically used to update the Cookie with the client. For example,
	 * expiring the Cookie to no longer be sent.
	 * 
	 * @param cookie
	 *            {@link HttpRequestCookie}.
	 * @return {@link HttpResponseCookie}.
	 */
	HttpResponseCookie setCookie(HttpRequestCookie cookie);

	/**
	 * <p>
	 * Sets a {@link HttpResponseCookie} from a {@link HttpRequestCookie}.
	 * <p>
	 * If a {@link HttpResponseCookie} already exists by the name, it is updated to
	 * the value and returned. Otherwise a new {@link HttpResponseCookie} is added
	 * for the name.
	 * 
	 * @param cookie
	 *            {@link HttpRequestCookie}
	 * @param initialiser
	 *            Enables initialising the attributes of the
	 *            {@link HttpResponseCookie}. This reduces locking in setting
	 *            multiple attributes.
	 * @return {@link HttpResponseCookie}.
	 */
	HttpResponseCookie setCookie(HttpRequestCookie cookie, Consumer<HttpResponseCookie> initialiser);

	/**
	 * Removes the {@link HttpResponseCookie}.
	 * 
	 * @param cookie
	 *            {@link HttpResponseCookie}.
	 * @return <code>true</code> if the {@link HttpResponseCookie} was removed.
	 */
	boolean removeCookie(HttpResponseCookie cookie);

	/**
	 * Obtains the {@link HttpResponseCookie} by name.
	 * 
	 * @param name
	 *            Name of the {@link HttpResponseCookie}.
	 * @return {@link HttpResponseCookie} or <code>null</code> if no
	 *         {@link HttpResponseCookie} by the name.
	 */
	HttpResponseCookie getCookie(String name);

}
