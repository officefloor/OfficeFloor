/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.servlet.socket.server.http;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Implementation of {@link ServerHttpConnection} backed by {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerHttpConnection implements ServerHttpConnection {

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest servletRequest;

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest httpRequest;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse httpResponse;

	/**
	 * Initiate.
	 * 
	 * @param servletRequest
	 *            {@link HttpServletRequest}.
	 * @param servletResponse
	 *            {@link HttpServletResponse}.
	 */
	public ServletServerHttpConnection(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		this.servletRequest = servletRequest;
		this.httpRequest = new ServletHttpRequest(this.servletRequest);
		this.httpResponse = new ServletHttpResponse(servletResponse);
	}

	/*
	 * ======================== ServerHttpConnection ======================
	 */

	@Override
	public boolean isSecure() {
		return this.servletRequest.isSecure();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		String localAddress = this.servletRequest.getLocalAddr();
		InetAddress address;
		try {
			address = InetAddress.getByName(localAddress);
		} catch (UnknownHostException ex) {
			address = null; // should never occur
		}
		int localPort = this.servletRequest.getLocalPort();
		return new InetSocketAddress(address, localPort);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		String remoteAddress = this.servletRequest.getRemoteAddr();
		InetAddress address;
		try {
			address = InetAddress.getByName(remoteAddress);
		} catch (UnknownHostException ex) {
			address = null; // should never occur
		}
		int remotePort = this.servletRequest.getRemotePort();
		return new InetSocketAddress(address, remotePort);
	}

	@Override
	public HttpRequest getHttpRequest() {
		return this.httpRequest;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.httpResponse;
	}

	@Override
	public String getHttpMethod() {
		return this.servletRequest.getMethod();
	}

	@Override
	public Serializable exportState() throws IOException {
		// TODO implement ServerHttpConnection.exportState
		throw new UnsupportedOperationException(
				"TODO implement ServerHttpConnection.exportState");
	}

	@Override
	public void importState(Serializable momento)
			throws IllegalArgumentException, IOException {
		// TODO implement ServerHttpConnection.importState
		throw new UnsupportedOperationException(
				"TODO implement ServerHttpConnection.importState");
	}

}