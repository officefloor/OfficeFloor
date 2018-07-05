/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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