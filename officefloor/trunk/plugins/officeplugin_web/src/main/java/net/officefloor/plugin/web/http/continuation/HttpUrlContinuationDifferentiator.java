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
package net.officefloor.plugin.web.http.continuation;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * <p>
 * Differentiator on a {@link Task} to specify it to service a URL continuation.
 * <p>
 * This allows routing {@link HttpRequest} instances to the appropriate
 * {@link Task} for servicing.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpUrlContinuationDifferentiator {

	/**
	 * Obtains the URI path for the application that the {@link Task} will
	 * service.
	 * 
	 * @return URI path for the application that the {@link Task} will service.
	 *         <code>null</code> will not register the {@link Task} for
	 *         servicing.
	 */
	String getApplicationUriPath();

	/**
	 * Indicates whether a secure {@link ServerHttpConnection} is required for
	 * servicing.
	 * 
	 * @return Flag indicating whether a redirect is required due to the
	 *         following:
	 *         <ul>
	 *         <li><code>true</code> to require a secure
	 *         {@link ServerHttpConnection}.</li>
	 *         <li><code>false</code> to require a non-secure
	 *         {@link ServerHttpConnection}.</li>
	 *         <li><code>null</code> will service regardless of whether there is
	 *         a secure {@link ServerHttpConnection} or not (no redirect will
	 *         occur).</li>
	 *         <ul>
	 */
	Boolean isSecure();

}