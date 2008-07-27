/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http.api;

import java.io.InputStream;
import java.util.Set;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel
 */
public interface HttpRequest {

	/**
	 * Obtains the HTTP method. For example GET, POST, etc.
	 * 
	 * @return HTTP method.
	 */
	String getMethod();

	/**
	 * Obtains the path as provided on the request.
	 * 
	 * @return Path as provided on the request.
	 */
	String getPath();

	/**
	 * Obtains the HTTP version of the request. For example HTTP/1.0, HTTP/1.1,
	 * etc.
	 * 
	 * @return HTTP version of the request.
	 */
	String getVersion();

	/**
	 * Obtains the set of HTTP header names on the request.
	 * 
	 * @return Set of HTTP header names on the request.
	 */
	Set<String> getHeaderNames();

	/**
	 * Obtains the HTTP header value.
	 * 
	 * @param name
	 *            Name of header value.
	 * @return Value or <code>null</code> if not provided in request.
	 */
	String getHeader(String name);

	/**
	 * Obtains the {@link InputStream} to the body of the HTTP request.
	 * 
	 * @return {@link InputStream} to the body of the HTTP request.
	 */
	InputStream getBody();

}
