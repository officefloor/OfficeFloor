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

/**
 * {@link HttpResponseCookie} instances for the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResponseCookies extends Iterable<HttpResponseCookie> {

	/**
	 * Adds a {@link HttpResponseCookie}.
	 * 
	 * @param cookie
	 *            {@link HttpResponseCookie}.
	 */
	HttpResponseCookie addCookie(String name, String value);

	/**
	 * <p>
	 * Adds a {@link HttpResponseCookie} from a {@link HttpRequestCookie}.
	 * <p>
	 * This is typically used to update the Cookie with the client. For example,
	 * expiring the Cookie to no longer be sent.
	 * 
	 * @param cookie
	 *            {@link HttpRequestCookie}.
	 * @return {@link HttpResponseCookie}.
	 */
	HttpResponseCookie addCookie(HttpRequestCookie cookie);

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