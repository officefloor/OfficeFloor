/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.cookie;

import java.net.HttpCookie;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * Utility methods for working with {@link HttpCookie} instances from the
 * {@link HttpRequest} to the {@link HttpResponse}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCookieUtil {

	/**
	 * Header name of a cookie.
	 */
	private static final String HEADER_NAME_COOKIE = "cookie";

	/**
	 * Extracts all the {@link HttpCookie} instances from the
	 * {@link HttpRequest}.
	 *
	 * @param request
	 *            {@link HttpRequest}.
	 * @return {@link HttpCookie} instances on the {@link HttpRequest}.
	 */
	public static List<HttpCookie> extractHttpCookies(HttpRequest request) {
		List<HttpCookie> cookies = new LinkedList<HttpCookie>();
		for (HttpHeader header : request.getHeaders()) {
			if (HEADER_NAME_COOKIE.equalsIgnoreCase(header.getName())) {
				cookies.addAll(HttpCookie.parse(header.getValue()));
			}
		}
		return cookies;
	}

	/**
	 * Extracts a specific {@link HttpCookie} from the {@link HttpRequest}.
	 *
	 * @param cookieName
	 *            Name of the {@link HttpCookie} to retrieve from the
	 *            {@link HttpRequest}.
	 * @param request
	 *            {@link HttpRequest}.
	 * @return {@link HttpCookie} extracted from the {@link HttpRequest} or
	 *         <code>null</code> if no {@link HttpCookie} by the name on the
	 *         {@link HttpRequest}.
	 */
	public static HttpCookie extractHttpCookie(String cookieName,
			HttpRequest request) {
		// Search for the cookie by the name
		for (HttpHeader header : request.getHeaders()) {
			if (HEADER_NAME_COOKIE.equalsIgnoreCase(header.getName())) {
				List<HttpCookie> cookies = HttpCookie.parse(header.getValue());
				for (HttpCookie cookie : cookies) {
					if (cookieName.equalsIgnoreCase(cookie.getName())) {
						// Found the cookie
						return cookie;
					}
				}
			}
		}

		// As here, cookie not found
		return null;
	}

	/**
	 * All access via static methods.
	 */
	private HttpCookieUtil() {
	}

}