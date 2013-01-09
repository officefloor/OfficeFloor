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
package net.officefloor.plugin.web.http.security.scheme;

import java.io.IOException;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Source for the {@link HttpSecurity} based on a particular authentication
 * scheme.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // TODO moving to HttpSecuritySource integrated into web application
public interface HttpSecuritySource<D extends Enum<D>> {

	/**
	 * Initialise this {@link HttpSecuritySource}.
	 * 
	 * @param context
	 *            {@link HttpSecuritySourceContext}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	void init(HttpSecuritySourceContext<D> context) throws Exception;

	/**
	 * Obtains the authentication scheme supported by this
	 * {@link HttpSecuritySource}.
	 * 
	 * @return Authentication scheme supported by this
	 *         {@link HttpSecuritySource}.
	 */
	String getAuthenticationScheme();

	/**
	 * Authenticates the {@link ServerHttpConnection}.
	 * 
	 * @param parameters
	 *            Parameters for authentication. In other words, the contents of
	 *            the <code>Authorization</code> {@link HttpHeader} after the
	 *            authentication scheme value.
	 * @param connection
	 *            {@link ServerHttpConnection} being authenticated.
	 * @param session
	 *            {@link HttpSession} to allow maintaining state between
	 *            {@link HttpRequest} instances.
	 * @param dependencies
	 *            Dependencies for authentication as specified in initialisation
	 *            on the {@link HttpSecuritySourceContext}.
	 * @return {@link HttpSecurity} or <code>null</code> if not authenticated.
	 * @throws IOException
	 *             If failure reading authentication information.
	 * @throws AuthenticationException
	 *             If failure in authentication.
	 */
	HttpSecurity authenticate(String parameters,
			ServerHttpConnection connection, HttpSession session,
			Map<D, Object> dependencies) throws IOException,
			AuthenticationException;

	/**
	 * Loads the unauthorised information to the {@link HttpResponse}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection} for the {@link HttpResponse}.
	 * @param session
	 *            {@link HttpSession} to allow maintaining state between
	 *            {@link HttpRequest} instances. This is especially useful for
	 *            security negotiations.
	 * @param dependencies
	 *            Dependencies for authentication as specified in initialisation
	 *            on the {@link HttpSecuritySourceContext}.
	 * @throws AuthenticationException
	 *             If failure in loading unauthorised information.
	 */
	void loadUnauthorised(ServerHttpConnection connection, HttpSession session,
			Map<D, Object> dependencies) throws AuthenticationException;

}