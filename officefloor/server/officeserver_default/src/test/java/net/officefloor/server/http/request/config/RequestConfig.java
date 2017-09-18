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
package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the HTTP request.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestConfig {

	/**
	 * HTTP method.
	 */
	public String method = "GET";

	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * URI path of the request line.
	 */
	public String path = "";

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * HTTP version.
	 */
	public String version = "HTTP/1.1";

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Headers.
	 */
	public final List<HeaderConfig> headers = new LinkedList<HeaderConfig>();

	public void addHeader(HeaderConfig header) {
		this.headers.add(header);
	}

	/**
	 * Body.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}
}
