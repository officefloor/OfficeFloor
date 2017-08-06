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

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Abstract {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractServerHttpConnection implements ServerHttpConnection {

	/*
	 * =================== ServerHttpConnection ========================
	 */

	@Override
	public HttpRequest getHttpRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse getHttpResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Serializable exportState() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importState(Serializable momento) throws IllegalArgumentException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpMethod getHttpMethod() {
		// TODO Auto-generated method stub
		return null;
	}

}
