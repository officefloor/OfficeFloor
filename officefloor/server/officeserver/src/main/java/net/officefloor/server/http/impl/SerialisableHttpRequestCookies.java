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
package net.officefloor.server.http.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;

/**
 * {@link Serializable} {@link HttpRequestCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequestCookies implements HttpRequestCookies {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link SerialisableHttpRequestCookie} instances.
	 */
	private final HttpRequestCookie[] cookies;

	/**
	 * Instantiate from {@link HttpRequestHeaders}.
	 * 
	 * @param httpRequestCookies
	 *            {@link HttpRequestCookies}.
	 */
	public SerialisableHttpRequestCookies(HttpRequestCookies httpRequestCookies) {
		this(httpRequestCookies.length(), httpRequestCookies);
	}

	/**
	 * Instantiate from {@link List} of {@link HttpRequestCookie} instances.
	 * 
	 * @param headers
	 *            {@link List} of {@link HttpRequestCookie} instances.
	 */
	public SerialisableHttpRequestCookies(List<HttpRequestCookie> cookies) {
		this(cookies.size(), cookies);
	}

	/**
	 * Loads the {@link HttpRequestCookie} instances.
	 * 
	 * @param length
	 *            Number of {@link HttpRequestCookie} instances.
	 * @param headers
	 *            {@link Iterable} over the {@link HttpRequestCookie} instances.
	 */
	protected SerialisableHttpRequestCookies(int length, Iterable<HttpRequestCookie> cookies) {
		this.cookies = new SerialisableHttpRequestCookie[length];
		int index = 0;
		for (HttpRequestCookie cookie : cookies) {
			this.cookies[index++] = new SerialisableHttpRequestCookie(cookie.getName(), cookie.getValue());
		}
	}

	/*
	 * ================== HttpRequestCookies ======================
	 */

	@Override
	public Iterator<HttpRequestCookie> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestCookie getCookie(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestCookie cookieAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

}