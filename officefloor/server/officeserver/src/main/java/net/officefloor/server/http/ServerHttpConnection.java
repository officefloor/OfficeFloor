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
import java.nio.charset.Charset;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * HTTP connection to be handled by the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServerHttpConnection {

	/**
	 * HTTP {@link Charset}.
	 */
	static Charset HTTP_CHARSET = Charset.forName("US-ASCII");

	/**
	 * URI {@link Charset}.
	 */
	static Charset URI_CHARSET = Charset.forName("UTF-8");

	/**
	 * HTTP entity default {@link Charset}.
	 */
	static Charset DEFAULT_HTTP_ENTITY_CHARSET = Charset.forName("UTF-8");

	/**
	 * Obtains the {@link HttpRequest} to be serviced.
	 * 
	 * @return {@link HttpRequest} to be serviced.
	 */
	HttpRequest getRequest();

	/**
	 * Obtains the {@link HttpResponse}.
	 * 
	 * @return {@link HttpResponse}.
	 */
	HttpResponse getResponse();

	/**
	 * Indicates if the connection is over a secure channel (e.g. utilising
	 * SSL).
	 * 
	 * @return <code>true</code> if connection is over a secure channel.
	 */
	boolean isSecure();

	/**
	 * Obtains the {@link HttpServerLocation}.
	 * 
	 * @return {@link HttpServerLocation}.
	 */
	HttpServerLocation getServerLocation();

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
	void importState(Serializable momento) throws IllegalArgumentException, IOException;

	/**
	 * <p>
	 * Obtains the actual client sent {@link HttpRequest} for the
	 * {@link ServerHttpConnection}.
	 * <p>
	 * As the {@link HttpRequest} can be overridden, this allows logic requiring
	 * to know details of the actual client {@link HttpRequest}. Examples of
	 * this logic are:
	 * <ul>
	 * <li>the POST/redirect/GET pattern that needs to know whether the client
	 * sent {@link HttpMethod} is a <code>POST</code> or <code>GET</code>
	 * (regardless of imported state)</li>
	 * <li>checking for the <code>Authorization</code> {@link HttpHeader} to
	 * ensure it was sent by the client for servicing the
	 * {@link HttpRequest}</li>
	 * <li>checking for the JWT token {@link HttpRequestCookie} to ensure it was
	 * sent by the client for servicing the {@link HttpRequest}</li>
	 * </ul>
	 * <p>
	 * Note for most application logic the {@link #getRequest()} should be used,
	 * as the intention is for this to contain the appropriate information for
	 * servicing the {@link HttpRequest}.
	 * 
	 * @return Actual client {@link HttpRequest}.
	 * 
	 * @see #exportState()
	 * @see #importState(Serializable)
	 */
	HttpRequest getClientRequest();

}