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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.application.WebApplicationAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.source.HttpSessionManagedObjectSource;

/**
 * {@link OfficeFloorSource} that extends the
 * {@link WebApplicationAutoWireOfficeFloorSource} to add the additional
 * functionality for running as a stand-alone HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerAutoWireOfficeFloorSource extends
		WebApplicationAutoWireOfficeFloorSource implements
		HttpServerAutoWireApplication {

	/**
	 * {@link HttpSocket} instances.
	 */
	private final List<HttpSocket> httpSockets = new LinkedList<HttpSocket>();

	/**
	 * {@link AutoWireObject} for the {@link HttpSession}.
	 */
	private final AutoWireObject httpSession;

	/**
	 * Initiate.
	 */
	public HttpServerAutoWireOfficeFloorSource() {
		// Use active team by default - done early so allow further overriding
		this.assignDefaultTeam(OnePersonTeamSource.class);

		// Configure HTTP Session (allowing 10 seconds to retrieve session)
		this.httpSession = this.addManagedObject(
				HttpSessionManagedObjectSource.class, null, HttpSession.class);
		this.httpSession.setTimeout(10 * 1000);
	}

	/**
	 * <p>
	 * Uses the configuration of this {@link HttpServerAutoWireApplication} to
	 * start the input {@link WebAutoWireApplication}.
	 * <p>
	 * Please note that the {@link WebAutoWireApplication} is not copied to the
	 * input {@link WebAutoWireApplication}.
	 * 
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @return {@link AutoWireOfficeFloor} of the started
	 *         {@link WebAutoWireApplication}.
	 * @throws Exception
	 *             If fails to start the {@link WebAutoWireApplication}.
	 */
	public AutoWireOfficeFloor startWebApplication(
			WebAutoWireApplication application) throws Exception {

		// Configure the web application
		this.configureWebApplication(application);

		// Return the started web application
		return application.openOfficeFloor();
	}

	/**
	 * Configures the {@link WebAutoWireApplication}.
	 * 
	 * @param application
	 *            {@link WebAutoWireApplication} to configure.
	 */
	@SuppressWarnings("unchecked")
	private void configureWebApplication(WebAutoWireApplication application) {

		// Add the HTTP Socket
		if (this.httpSockets.size() == 0) {
			// Add the default HTTP Socket
			HttpServerSocketManagedObjectSource.autoWire(application, 7878,
					HANDLER_SECTION_NAME, HANDLER_INPUT_NAME);
		} else {
			// Override the HTTP Socket
			for (HttpSocket socket : this.httpSockets) {
				AutoWireObject object = application.addManagedObject(
						socket.managedObjectSource, socket.wirer,
						ServerHttpConnection.class);
				for (Property property : socket.properties) {
					object.addProperty(property.getName(), property.getValue());
				}
			}
		}
	}

	/*
	 * ===================== HttpServerAutoWireApplication ===================
	 */

	@Override
	public <D extends Enum<D>, F extends Enum<F>, M extends ManagedObjectSource<D, F>> PropertyList addHttpSocket(
			Class<M> managedObjectSource, ManagedObjectSourceWirer wirer) {

		// Create the properties
		PropertyList properties = this.getOfficeFloorCompiler()
				.createPropertyList();

		// Create and register the HTTP socket
		HttpSocket httpSocket = new HttpSocket(managedObjectSource, wirer,
				properties);
		this.httpSockets.add(httpSocket);

		// Return the properties
		return properties;
	}

	@Override
	public AutoWireObject getHttpSessionAutoWireObject() {
		return this.httpSession;
	}

	/*
	 * ===================== AutoWireOfficeFloorSource =======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Initiate this web application
		super.initOfficeFloor(deployer, context);

		// Configure this web application
		this.configureWebApplication(this);
	}

	/**
	 * Configuration of a HTTP socket.
	 */
	private static class HttpSocket {

		/**
		 * {@link ManagedObjectSource} class.
		 */
		@SuppressWarnings("rawtypes")
		public final Class managedObjectSource;

		/**
		 * {@link ManagedObjectSourceWirer}.
		 */
		public final ManagedObjectSourceWirer wirer;

		/**
		 * {@link PropertyList}.
		 */
		public final PropertyList properties;

		/**
		 * Initiate.
		 * 
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource} class.
		 * @param wirer
		 *            {@link ManagedObjectSourceWirer}.
		 * @param properties
		 *            {@link PropertyList}.
		 */
		public HttpSocket(Class<?> managedObjectSource,
				ManagedObjectSourceWirer wirer, PropertyList properties) {
			this.managedObjectSource = managedObjectSource;
			this.wirer = wirer;
			this.properties = properties;
		}
	}

}