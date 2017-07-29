/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.io.IOException;
import java.util.List;

import net.officefloor.server.stream.ServerInputStream;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequest {

	/**
	 * Obtains the HTTP method. For example GET, POST, etc.
	 * 
	 * @return HTTP method.
	 */
	String getMethod();

	/**
	 * Obtains the Request URI as provided on the request.
	 * 
	 * @return Request URI as provided on the request.
	 */
	String getRequestURI();

	/**
	 * Obtains the HTTP version of the request. For example HTTP/1.0, HTTP/1.1,
	 * etc.
	 * 
	 * @return HTTP version of the request.
	 */
	String getVersion();

	/**
	 * Obtains the {@link HttpHeader} instances in the order they appear on the
	 * request.
	 * 
	 * @return {@link HttpHeader} instances in the order they appear on the
	 *         request.
	 */
	List<HttpHeader> getHeaders();

	/**
	 * Obtains the {@link ServerInputStream} to the entity of the HTTP request.
	 * 
	 * @return {@link ServerInputStream} to the entity of the HTTP request.
	 * @throws IOException
	 *             If failure in reading the entity.
	 */
	ServerInputStream getEntity() throws IOException;

}