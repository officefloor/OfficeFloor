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
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * HTTP connection to be handled by the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServerHttpConnection {

	/**
	 * Obtains the {@link HttpRequest} for this {@link ServerHttpConnection}.
	 * 
	 * @return {@link HttpRequest} for this {@link ServerHttpConnection}.
	 */
	HttpRequest getHttpRequest();

	/**
	 * Obtains the {@link HttpResponse} for this {@link ServerHttpConnection}.
	 * 
	 * @return {@link HttpResponse} for this {@link ServerHttpConnection}.
	 */
	HttpResponse getHttpResponse();

	/**
	 * Indicates if the connection is over a secure channel (e.g. utilising
	 * SSL).
	 * 
	 * @return <code>true</code> if connection is over a secure channel.
	 */
	boolean isSecure();

	/**
	 * Obtains the local address for this {@link ServerHttpConnection}.
	 * 
	 * @return {@link InetSocketAddress} describing the local {@link Socket} for
	 *         this {@link ServerHttpConnection}.
	 */
	InetSocketAddress getLocalAddress();

	/**
	 * Obtains the remote address for this {@link ServerHttpConnection}.
	 * 
	 * @return {@link InetSocketAddress} describing the remote {@link Socket}
	 *         for this {@link ServerHttpConnection}.
	 */
	InetSocketAddress getRemoteAddress();

	/**
	 * <p>
	 * Exports the state of the current {@link HttpRequest} and
	 * {@link HttpResponse}.
	 * <p>
	 * This enables maintaining the state of the {@link HttpRequest} /
	 * {@link HttpResponse} and later reinstating them (typically after a
	 * redirect).
	 * 
	 * @return Momento containing the current {@link HttpRequest} and
	 *         {@link HttpResponse} state.
	 * @throws IOException
	 *             Should the state not be able to be exported.
	 * 
	 * @see #importState(Serializable)
	 */
	Serializable exportState() throws IOException;

	/**
	 * Imports and overrides the current {@link HttpRequest} and
	 * {@link HttpResponse} with the input momento.
	 * 
	 * @param momento
	 *            Momento exported from a {@link ServerHttpConnection}.
	 * @throws IllegalArgumentException
	 *             Should the momento be invalid.
	 * @throws IOException
	 *             Should the state not be able to be imported.
	 * 
	 * @see #exportState()
	 */
	void importState(Serializable momento) throws IllegalArgumentException,
			IOException;

	/**
	 * <p>
	 * Obtains the client sent HTTP method of the {@link ServerHttpConnection}.
	 * <p>
	 * As the {@link HttpRequest} method is overridden, this method may be used
	 * by logic requiring to know the actual client HTTP method. An example of
	 * this logic is the POST/redirect/GET pattern that needs to know whether
	 * the client sent {@link HttpRequest} method is a <code>POST</code> or
	 * <code>GET</code> (regardless of imported state).
	 * 
	 * @return Client sent HTTP method.
	 * 
	 * @see #exportState()
	 * @see #importState(Serializable)
	 */
	String getHttpMethod();

}