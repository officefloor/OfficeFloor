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

import java.util.Iterator;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.WritableHttpCookie;

/**
 * {@link ProcessAwareContext} {@link HttpResponseCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseCookies implements HttpResponseCookies {

	/**
	 * Head {@link WritableHttpCookie} instance.
	 */
	private WritableHttpCookie head = null;

	/**
	 * Tail {@link WritableHttpCookie} instance.
	 */
	private WritableHttpCookie tail = null;

	/**
	 * Count of the number of Cookies.
	 */
	private int cookieCount = 0;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public ProcessAwareHttpResponseCookies(ProcessAwareContext context) {
		this.context = context;
	}

	/**
	 * Obtains the head {@link WritableHttpCookie} to the linked list of
	 * {@link WritableHttpCookie} instances for the {@link HttpResponseWriter}.
	 * 
	 * @return Head {@link WritableHttpCookie} to the linked list of
	 *         {@link WritableHttpCookie} instances for the
	 *         {@link HttpResponseWriter}. May be <code>null</code>.
	 */
	public WritableHttpCookie getWritableHttpCookie() {
		return this.head;
	}

	/*
	 * ================= HttpResponseCookies =======================
	 */

	@Override
	public Iterator<HttpResponseCookie> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponseCookie addCookie(String name, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponseCookie addCookie(HttpRequestCookie cookie) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeCookie(HttpResponseCookie cookie) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HttpResponseCookie getCookie(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}