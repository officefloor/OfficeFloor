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
package net.officefloor.plugin.servlet.host;

import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;
import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * <p>
 * Provides the server hosting information/functionality for the {@link Servlet}
 * application.
 * <p>
 * This provides the information/functionality that is provided by the
 * Application Server to a {@link Servlet} application (e.g. configuration
 * typically not stored in the <code>web.xml</code> file).
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletServer {

	/**
	 * Obtains the name of the Server.
	 * 
	 * @return Server name.
	 */
	String getServerName();

	/**
	 * Obtains the port the Server is listening for a {@link HttpRequest}.
	 * 
	 * @return Server port.
	 */
	int getServerPort();

	/**
	 * Obtains the context path for the {@link Servlet} application.
	 * 
	 * @return Context path for the {@link Servlet} application.
	 */
	String getContextPath();

	/**
	 * Obtains the {@link ResourceLocator} for the {@link Servlet} application.
	 * 
	 * @return {@link ResourceLocator} for the {@link Servlet} application.
	 */
	ResourceLocator getResourceLocator();

	/**
	 * Obtains the {@link Logger} for the {@link Servlet} application.
	 * 
	 * @return {@link Logger} for the {@link Servlet} application.
	 */
	Logger getLogger();

}