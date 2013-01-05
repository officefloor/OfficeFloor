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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.ssl.OfficeFloorDefaultSslEngineSource;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;
import net.officefloor.plugin.web.http.application.HttpApplicationState;
import net.officefloor.plugin.web.http.application.HttpApplicationStateManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateManagedObjectSource;
import net.officefloor.plugin.web.http.application.WebApplicationAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;

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
			1);

	/**
	 * Added HTTPS ports. Typically HTTPS ports will be added through
	 * {@link Property} configuration.
	 */
	private final Map<Integer, AutoWireObject> addedHttpsPorts = new HashMap<Integer, AutoWireObject>(
			1);

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
		this(httpPort, -1, null);
	}

	/**
	 * Initiate to use the specified HTTP port.
	 * 
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @param sslEngineSourceClass
	 *            {@link SslEngineSource} class. May be <code>null</code>.
	 */
	public HttpServerAutoWireOfficeFloorSource(int httpPort, int httpsPort,
			Class<? extends SslEngineSource> sslEngineSourceClass) {

		// Obtain the OfficeFloor compiler
		OfficeFloorCompiler compiler = this.getOfficeFloorCompiler();

		// Configure to use the HTTP port
		if (httpPort > 0) {
			compiler.addProperty(
					HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
					String.valueOf(httpPort));
		}

		// Configure to use the HTTPS port
		if (httpsPort > 0) {
			compiler.addProperty(
					HttpApplicationLocationManagedObjectSource.PROPERTY_CLUSTER_HTTPS_PORT,
					String.valueOf(httpsPort));
			if (sslEngineSourceClass != null) {
				compiler.addProperty(
						SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
						sslEngineSourceClass.getName());
			}
		}

		// Use passive team by default (saves on context switching)
		this.assignDefaultTeam(PassiveTeamSource.class.getName());

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
			this.addedHttpPorts.put(portInteger, object);
		}

		// Return the HTTP Server Socket
		return object;
	}

	@Override
	public AutoWireObject addHttpsServerSocket(int port,
			Class<? extends SslEngineSource> sslEngineSourceClass) {

		// Lazy create the HTTPS Server Socket on the port
		Integer portInteger = Integer.valueOf(port);
		AutoWireObject object = this.addedHttpsPorts.get(portInteger);
		if (object == null) {

			// Create the HTTPS Server Socket on the port
			object = HttpsServerSocketManagedObjectSource.autoWire(this, port,
					sslEngineSourceClass, HANDLER_SECTION_NAME,
					HANDLER_INPUT_NAME);

			// Register adding the HTTPS port
			this.addedHttpsPorts.put(portInteger, object);
		}

		// Return the HTTPS Server Socket
		return object;
	}

	@Override
	public AutoWireObject getHttpSessionAutoWireObject() {
		return this.httpSession;
	}

	/*
	 * ===================== AutoWireOfficeFloorSource =======================
	 */

	@Override
	@SuppressWarnings("unchecked")
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

		// Add the configured HTTPS port
		String httpsPort = context
				.getProperty(
						HttpApplicationLocationManagedObjectSource.PROPERTY_CLUSTER_HTTPS_PORT,
						context.getProperty(
								HttpApplicationLocationManagedObjectSource.PROPERTY_HTTPS_PORT,
								null));
		if (httpsPort != null) {
			// Determine if SSL Engine Configurator configured
			String sslEngineConfiguratorClassName = context.getProperty(
					SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE, null);
			Class<? extends SslEngineSource> sslEngineConfiguratorClass = null;
			if (sslEngineConfiguratorClassName != null) {
				sslEngineConfiguratorClass = (Class<? extends SslEngineSource>) context
						.loadClass(sslEngineConfiguratorClassName);
			}

			// Add the configured HTTPS port
			this.addHttpsServerSocket(Integer.parseInt(httpsPort),
					sslEngineConfiguratorClass);

		} else if (this.addedHttpsPorts.size() == 0) {
			// Provide default HTTPS port (focus on ease of development)
			this.addHttpsServerSocket(
					HttpApplicationLocationManagedObjectSource.DEFAULT_HTTPS_PORT,
					OfficeFloorDefaultSslEngineSource.class);
		}
	}

}