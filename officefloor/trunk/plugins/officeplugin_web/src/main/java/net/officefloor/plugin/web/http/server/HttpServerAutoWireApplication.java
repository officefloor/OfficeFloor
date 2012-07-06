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

package net.officefloor.plugin.web.http.server;

import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.source.HttpSessionManagedObjectSource;

/**
 * Auto-wired web application that is stand-alone.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServerAutoWireApplication extends WebAutoWireApplication {

	/**
	 * <p>
	 * Allows overriding the {@link ManagedObjectSource} providing the
	 * {@link ServerHttpConnection}.
	 * <p>
	 * This may be called multiple times to add listeners on more than one port.
	 * 
	 * @param managedObjectSourceClassName
	 *            {@link ManagedObjectSource} providing the
	 *            {@link ServerHttpConnection}. May be an alias.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}. May be <code>null</code>.
	 * @return {@link PropertyList} for configuring the
	 *         {@link ManagedObjectSource}.
	 * 
	 * @deprecated use addHttpSocket and addHttpsSocket
	 * 
	 * @see #HANDLER_SECTION_NAME
	 * @see #HANDLER_INPUT_NAME
	 */
	PropertyList addHttpSocket(String managedObjectSourceClassName,
			ManagedObjectSourceWirer wirer);

	/**
	 * <p>
	 * Adds listening for HTTP on the specified port.
	 * <p>
	 * The first added port will be the HTTP port for the
	 * {@link HttpApplicationLocation}. Note configuring the HTTP port location
	 * through properties overrides this behaviour.
	 * 
	 * @param port
	 *            Port to listen on.
	 */
	// void addHttpSocket(int port);

	/**
	 * <p>
	 * Adds listening for HTTPS on the specified port.
	 * <p>
	 * The first added port will be the HTTPS port for the
	 * {@link HttpApplicationLocation}. Note configuring the HTTPS port location
	 * through properties overrides this behaviour.
	 * 
	 * @param port
	 *            Port to listen on.
	 */
	// void addHttpsSocket(int port);

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