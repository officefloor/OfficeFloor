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
package net.officefloor.plugin.web.http.server;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
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
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} providing the
	 *            {@link ServerHttpConnection}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}. May be <code>null</code>.
	 * @return {@link PropertyList} for configuring the
	 *         {@link ManagedObjectSource}.
	 * 
	 * @see #HANDLER_SECTION_NAME
	 * @see #HANDLER_INPUT_NAME
	 */
	<D extends Enum<D>, F extends Enum<F>, M extends ManagedObjectSource<D, F>> PropertyList addHttpSocket(
			Class<M> managedObjectSource, ManagedObjectSourceWirer wirer);

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