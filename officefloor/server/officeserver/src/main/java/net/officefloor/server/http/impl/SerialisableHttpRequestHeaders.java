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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestHeaders;

/**
 * {@link Serializable} {@link HttpRequestHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequestHeaders implements HttpRequestHeaders, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link SerialisableHttpHeader} instances.
	 */
	private final HttpHeader[] headers;

	/**
	 * Instantiate from {@link HttpRequestHeaders}.
	 * 
	 * @param httpRequestHeaders
	 *            {@link HttpRequestHeaders}.
	 */
	public SerialisableHttpRequestHeaders(HttpRequestHeaders httpRequestHeaders) {
		this(httpRequestHeaders.length(), httpRequestHeaders);
	}

	/**
	 * Instantiate from {@link List} of {@link HttpHeader} instances.
	 * 
	 * @param headers
	 *            {@link List} of {@link HttpHeader} instances.
	 */
	public SerialisableHttpRequestHeaders(List<HttpHeader> headers) {
		this(headers.size(), headers);
	}

	/**
	 * Loads the {@link HttpHeader} instances.
	 * 
	 * @param length
	 *            Number of {@link HttpHeader} instances.
	 * @param headers
	 *            {@link Iterable} over the {@link HttpHeader} instances.
	 */
	protected SerialisableHttpRequestHeaders(int length, Iterable<HttpHeader> headers) {
		this.headers = new SerialisableHttpHeader[length];
		int index = 0;
		for (HttpHeader header : headers) {
			this.headers[index++] = new SerialisableHttpHeader(header.getName(), header.getValue());
		}
	}

	/*
	 * ================= HttpRequestHeaders ======================
	 */

	@Override
	public Iterator<HttpHeader> iterator() {
		return Arrays.asList(this.headers).iterator();
	}

	@Override
	public HttpHeader getHeader(CharSequence name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<HttpHeader> getHeaders(CharSequence name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpHeader headerAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

}