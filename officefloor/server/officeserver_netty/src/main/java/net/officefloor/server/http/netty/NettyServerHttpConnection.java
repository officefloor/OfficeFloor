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

import java.io.IOException;
import java.io.Serializable;

import io.netty.channel.ChannelHandlerContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Netty {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyServerHttpConnection implements ServerHttpConnection {

	/**
	 * {@link NettyHttpRequest}.
	 */
	private final NettyHttpRequest httpRequest;

	/**
	 * {@link NettyHttpResponse}.
	 */
	private final NettyHttpResponse httpResponse;

	/**
	 * Indicates if secure.
	 */
	private final boolean isSecure;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ChannelHandlerContext}.
	 * @param request
	 *            {@link io.netty.handler.codec.http.HttpRequest}.
	 */
	public NettyServerHttpConnection(ChannelHandlerContext context, io.netty.handler.codec.http.HttpRequest request,
			boolean isSecure) {
		this.httpRequest = new NettyHttpRequest(request);
		this.httpResponse = new NettyHttpResponse(context);
		this.isSecure = isSecure;
	}

	/*
	 * =================== ServerHttpConnection ======================
	 */

	@Override
	public HttpRequest getHttpRequest() {
		return this.httpRequest;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.httpResponse;
	}

	@Override
	public boolean isSecure() {
		return this.isSecure;
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
		return this.httpRequest.getHttpMethod();
	}

}
