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
package net.officefloor.server.http.mock;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;

/**
 * Mock {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpResponse {

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpStatus}.
	 * 
	 * @return {@link HttpStatus}.
	 */
	HttpStatus getStatus();

	/**
	 * Obtains the first {@link WritableHttpHeader} by the name.
	 * 
	 * @param name
	 *            Name of the {@link WritableHttpHeader}.
	 * @return First {@link WritableHttpHeader} by the name, or
	 *         <code>null</code> if no {@link WritableHttpHeader} by the name.
	 */
	WritableHttpHeader getHeader(String name);

	/**
	 * Obtains the response {@link WritableHttpHeader} instances.
	 * 
	 * @return {@link WritableHttpHeader} instances.
	 */
	List<WritableHttpHeader> getHeaders();

	/**
	 * Obtains the response {@link WritableHttpCookie} instances.
	 * 
	 * @return {@link WritableHttpCookie} instances.
	 */
	List<WritableHttpCookie> getCookies();

	/**
	 * Obtains {@link InputStream} to the response HTTP entity.
	 * 
	 * @return {@link InputStream} to the response HTTP entity.
	 */
	InputStream getEntity();

	/**
	 * Obtains the HTTP entity as text.
	 * 
	 * @param charset
	 *            {@link Charset} for HTTP entity. May be <code>null</code> to
	 *            use default {@link Charset}.
	 * @return Text of the HTTP entity.
	 */
	String getEntity(Charset charset);

}