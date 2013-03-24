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
package net.officefloor.plugin.servlet.socket.server.http.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.socket.server.http.ServletServerHttpConnection;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link ManagedObjectSource} for the {@link ServletServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerHttpConnectionManagedObjectSource
		extends
		AbstractManagedObjectSource<ServletServerHttpConnectionManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		SERVLET_BRIDGE
	}

	/*
	 * ====================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context)
			throws Exception {

		// Provide meta-data
		context.setObjectClass(ServerHttpConnection.class);
		context.setManagedObjectClass(ServletServerHttpConnectionManagedObject.class);

		// Requires access to the Servlet bridge
		context.addDependency(DependencyKeys.SERVLET_BRIDGE,
				ServletBridge.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ServletServerHttpConnectionManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link ServletServerHttpConnection}.
	 */
	private static class ServletServerHttpConnectionManagedObject implements
			CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link ServletServerHttpConnection}.
		 */
		private ServletServerHttpConnection connection;

		/*
		 * ==================== ManagedObject ===========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry)
				throws Throwable {

			// Obtain the servlet details
			ServletBridge bridge = (ServletBridge) registry
					.getObject(DependencyKeys.SERVLET_BRIDGE);

			// Create the connection
			this.connection = new ServletServerHttpConnection(
					bridge.getRequest(), bridge.getResponse());
		}

		@Override
		public Object getObject() throws Throwable {
			return this.connection;
		}
	}

}