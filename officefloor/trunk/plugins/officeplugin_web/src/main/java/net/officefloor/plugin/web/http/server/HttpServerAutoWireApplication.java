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
package net.officefloor.plugin.web.http.server;

import net.officefloor.autowire.AutoWireObject;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;

/**
 * Auto-wired web application that is stand-alone.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerAutoWireApplication extends WebAutoWireApplication {

	/**
	 * Adds listening for HTTP on the specified port.
	 * 
	 * @param port
	 *            Port to listen on.
	 * @return {@link AutoWireObject} of the added HTTP server socket.
	 */
	AutoWireObject addHttpServerSocket(int port);

	/**
	 * Adds listening for HTTPS on the specified port.
	 * 
	 * @param port
	 *            Port to listen on.
	 * @param sslEngineSourceClass
	 *            {@link SslEngineSource} class. May be <code>null</code>.
	 * @return {@link AutoWireObject} of the added HTTPS server socket.
	 */
	AutoWireObject addHttpsServerSocket(int port,
			Class<? extends SslEngineSource> sslEngineSourceClass);

	/**
	 * <p>
	 * Obtains the {@link AutoWireObject} for the {@link HttpSession}.
	 * <p>
	 * This allows overriding the default configuration for the
	 * {@link HttpSessionManagedObjectSource}.
	 * 
	 * @return {@link AutoWireObject} for the {@link HttpSession}.
	 */
	AutoWireObject getHttpSessionAutoWireObject();

}