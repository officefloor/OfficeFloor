/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http;

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
	 * Indicates if the connection is over a secure channel (e.g. utilising SSL).
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

}