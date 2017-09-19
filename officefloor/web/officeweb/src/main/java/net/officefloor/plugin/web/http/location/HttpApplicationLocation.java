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
package net.officefloor.plugin.web.http.location;

import net.officefloor.server.http.HttpRequest;

/**
 * Location details of the HTTP application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpApplicationLocation {

	/**
	 * <p>
	 * Obtains the domain for the application.
	 * <p>
	 * This is as the client will see the application.
	 * 
	 * @return Domain for the application.
	 */
	String getDomain();

	/**
	 * <p>
	 * Obtains the HTTP port.
	 * <p>
	 * This is as the client will see the application.
	 * 
	 * @return HTTP port.
	 */
	int getHttpPort();

	/**
	 * <p>
	 * Obtains the HTTPS port.
	 * <p>
	 * This is as the client will see the application.
	 * 
	 * @return HTTPS port.
	 */
	int getHttpsPort();

	/**
	 * Obtains the URI prefix to the application.
	 * 
	 * @return URI prefix to the application. May be <code>null</code> if no
	 *         context path for the application.
	 */
	String getContextPath();

	/**
	 * Obtains the name of the host for the application within the cluster. This
	 * name should be understood by all nodes within the cluster.
	 * 
	 * @return Name of the host within the cluster.
	 */
	String getClusterHostName();

	/**
	 * The cluster may be behind a load balancer and the application may be
	 * running on a different port than expected by the client.
	 * 
	 * @return Actual port on the cluster host the application is running on.
	 */
	int getClusterHttpPort();

	/**
	 * The cluster may be behind a load balancer and the application may be
	 * running on a different port than expected by the client.
	 * 
	 * @return Actual secure port on the cluster host the application is running
	 *         on.
	 */
	int getClusterHttpsPort();

	/**
	 * Transforms the {@link HttpRequest} request URI to a canonical path
	 * relative to the application root. This will strip off the domain, port
	 * and context path.
	 * 
	 * @param requestUri
	 *            Request URI from the {@link HttpRequest}.
	 * @return Canonical path relative to the application root.
	 * @throws InvalidHttpRequestUriException
	 *             Should the {@link HttpRequest} request URI be invalid.
	 * @throws IncorrectHttpRequestContextPathException
	 *             Should the {@link HttpRequest} request URI have the incorrect
	 *             context path for the application.
	 */
	String transformToApplicationCanonicalPath(String requestUri)
			throws InvalidHttpRequestUriException,
			IncorrectHttpRequestContextPathException;

	/**
	 * Transforms the application path to a client path. It will prepend the
	 * path with the application context path and if necessary provides
	 * HTTP/HTTPS protocol and domain.
	 * 
	 * @param applicationPath
	 *            Path relative to the application root.
	 * @param isSecure
	 *            Indicates if the resulting client path is to be secure. In
	 *            other words to use the HTTPS protocol.
	 * @return Client path.
	 */
	String transformToClientPath(String applicationPath, boolean isSecure);

}