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

import net.officefloor.server.stream.ServerInputStream;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequest {

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getMethod();

	/**
	 * Obtains the request URI as provided on the request.
	 * 
	 * @return Request URI as provided on the request.
	 */
	String getUri();

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpRequestHeaders}.
	 * 
	 * @return {@link HttpRequestHeaders}.
	 */
	HttpRequestHeaders getHeaders();

	/**
	 * Obtains the {@link HttpRequestCookies}.
	 * 
	 * @return {@link HttpRequestCookies}.
	 */
	HttpRequestCookies getCookies();

	/**
	 * Obtains the {@link ServerInputStream} to the entity of the HTTP request.
	 * 
	 * @return {@link ServerInputStream} to the entity of the HTTP request.
	 */
	ServerInputStream getEntity();

}