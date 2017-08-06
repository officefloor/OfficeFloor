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

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerInputStream;

/**
 * Abstract {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractHttpRequest implements HttpRequest {

	/*
	 * ================= HttpRequest =====================
	 */

	@Override
	public HttpMethod getHttpMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpVersion getHttpVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestHeaders getHttpHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerInputStream getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}
