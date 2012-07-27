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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpRequestHandlerMarker;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateManagedObjectSource;
import net.officefloor.plugin.web.http.application.WebApplicationAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
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
	 * {@link AutoWireObject} for the {@link HttpSession}.
	 */
	private final AutoWireObject httpSession;

	/**
	 * Added HTTP ports. Typically HTTP ports will be added through
	 * {@link Property} configuration.
	 */
	private final Map<Integer, AutoWireObject> addedHttpPorts = new HashMap<Integer, AutoWireObject>(
			0);

	/**
	 * Initiate.
	 */
	public HttpServerAutoWireOfficeFloorSource() {
		this(-1);
	}

	/**
	 * Initiate to use the specified HTTP port.
	 * 
	 * @param httpPort
	 *            HTTP port.
	 */
	public HttpServerAutoWireOfficeFloorSource(int httpPort) {

		// Configure to use the HTTP port
		if (httpPort > 0) {
			this.getOfficeFloorCompiler()
					.addProperty(
							HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
							String.valueOf(httpPort));
		}

		// Use passive team by default (saves on context switching)
		this.assignDefaultTeam(PassiveTeamSource.class.getName());

		// Use cached team to handle HTTP requests
		this.assignTeam(ExecutorCachedTeamSource.class.getName(), new AutoWire(
				HttpRequestHandlerMarker.class));

		// Configure HTTP Session (allowing 10 seconds to retrieve session)
		this.httpSession = this.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class));
		this.httpSession.setTimeout(10 * 1000);

		// Configure the HTTP Application and Request States
		this.addManagedObject(
				HttpApplicationStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpApplicationState.class));
		this.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));
	}

	/*
	 * ===================== HttpServerAutoWireApplication ===================
	 */

	@Override
	public AutoWireObject addHttpServerSocket(int port) {

		// Lazy create the HTTP Server Socket on the port
		Integer portInteger = Integer.valueOf(port);
		AutoWireObject object = this.addedHttpPorts.get(portInteger);
		if (object == null) {

			// Create the HTTP Server Socket on the port
			object = HttpServerSocketManagedObjectSource.autoWire(this, port,
					HANDLER_SECTION_NAME, HANDLER_INPUT_NAME);

			// Register adding the HTTP port
			this.addedHttpPorts.put(Integer.valueOf(port), object);
		}

		// Return the HTTP Server Socket
		return object;
	}

	@Override
	public AutoWireObject addHttpsServerSocket(int port) {
		// TODO implement HttpServerAutoWireApplication.addHttpsSocket
		throw new UnsupportedOperationException(
				"TODO implement HttpServerAutoWireApplication.addHttpsSocket");
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

		// Add the configured HTTP port
		String httpPort = context
				.getProperty(
						HttpApplicationLocationManagedObjectSource.PROPERTY_CLUSTER_HTTP_PORT,
						context.getProperty(
								HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
								null));
		if (httpPort != null) {
			// Add the configured HTTP port
			this.addHttpServerSocket(Integer.parseInt(httpPort));

		} else if (this.addedHttpPorts.size() == 0) {
			// Provide default HTTP port
			this.addHttpServerSocket(HttpApplicationLocationManagedObjectSource.DEFAULT_HTTP_PORT);
		}
	}

}