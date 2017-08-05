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
package net.officefloor.server.http.netty;

import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerInputStream;

/**
 * Netty {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyHttpRequest implements HttpRequest {

	/**
	 * {@link io.netty.handler.codec.http.HttpRequest}.
	 */
	private final io.netty.handler.codec.http.HttpRequest nettyHttpRequest;

	/**
	 * Instantiate.
	 * 
	 * @param nettyHttpRequest
	 *            {@link io.netty.handler.codec.http.HttpRequest}.
	 */
	public NettyHttpRequest(io.netty.handler.codec.http.HttpRequest nettyHttpRequest) {
		this.nettyHttpRequest = nettyHttpRequest;
	}

	/**
	 * ============== HttpReqeust ==================
	 */

	@Override
	public HttpMethod getHttpMethod() {
		return new HttpMethod(this.nettyHttpRequest.method().name());
	}

	@Override
	public String getRequestURI() {
		return this.nettyHttpRequest.uri();
	}

	@Override
	public HttpVersion getHttpVersion() {
		return new HttpVersion(this.nettyHttpRequest.protocolVersion().protocolName());
	}

	@Override
	public List<HttpHeader> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerInputStream getEntity() {
		// TODO Auto-generated method stub
		return null;
	}
}
