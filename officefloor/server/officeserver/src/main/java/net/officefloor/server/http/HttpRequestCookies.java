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

/**
 * {@link HttpRequestCookie} instances for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestCookies extends Iterable<HttpRequestCookie> {

	/**
	 * Obtains the first {@link HttpRequestCookie} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpRequestCookie}.
	 * @return First {@link HttpRequestCookie} or <code>null</code> if no
	 *         {@link HttpRequestCookie} by the name.
	 */
	HttpRequestCookie getCookie(String name);

	/**
	 * Obtains the {@link HttpRequestCookie} at the index.
	 * 
	 * @param index
	 *            Index of the {@link HttpRequestCookie}.
	 * @return {@link HttpRequestCookie} at the index.
	 */
	HttpRequestCookie cookieAt(int index);

	/**
	 * Obtains the number of {@link HttpRequestCookie} instances.
	 * 
	 * @return Number of {@link HttpRequestCookie} instances.
	 */
	int length();

}
