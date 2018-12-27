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
package net.officefloor.web.resource.source;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.route.WebServicer;

/**
 * Path for a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPath {

	/**
	 * Path.
	 */
	private final String path;

	/**
	 * {@link WebServicer}.
	 */
	private final WebServicer webServicer;

	/**
	 * Instantiate.
	 * 
	 * @param path Path.
	 */
	public HttpPath(String path) {
		this.path = path;
		this.webServicer = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpRequest} to extract the path.
	 */
	public HttpPath(HttpRequest request, WebServicer webServicer) {
		this.path = request.getUri();
		this.webServicer = webServicer;
	}

	/**
	 * Obtains the path.
	 * 
	 * @return Path.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Obtains the {@link WebServicer}.
	 * 
	 * @return {@link WebServicer}.
	 */
	public WebServicer getWebServicer() {
		return this.webServicer;
	}

}