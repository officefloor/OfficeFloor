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
package net.officefloor.plugin.socket.server.http.source;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeamSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocolSource;
import net.officefloor.plugin.socket.server.ssl.SslEngineSource;
import net.officefloor.plugin.socket.server.ssl.protocol.SslCommunicationProtocol;

/**
 * {@link ManagedObjectSource} for a secure {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpsServerSocketManagedObjectSource extends
		AbstractServerSocketManagedObjectSource
		implements
		ManagedObjectSourceService<None, Indexed, HttpsServerSocketManagedObjectSource> {

	/**
	 * Convenience method to create the {@link ManagedObjectSourceWirer} for
	 * wiring in the {@link HttpsServerSocketManagedObjectSource}.
	 * 
	 * @param sectionName
	 *            Name of the section handling the {@link HttpRequest}.
	 * @param sectionInputName
	 *            Name of the {@link SectionInput} handling the
	 *            {@link HttpRequest}.
	 * @return {@link ManagedObjectSourceWirer} for wiring in the
	 *         {@link HttpsServerSocketManagedObjectSource}.
	 */
	public static ManagedObjectSourceWirer createManagedObjectSourceWirer(
			final String sectionName, final String sectionInputName) {
		return new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Provide thread per each accepter and listener
				context.mapTeam("accepter",
						WorkerPerTaskTeamSource.class.getName());
				context.mapTeam("listener",
						WorkerPerTaskTeamSource.class.getName());

				// Clean up (without thread context switch)
				context.mapTeam("cleanup", PassiveTeamSource.class.getName());

				// Service SSL tasks within own thread
				context.mapTeam("SSL_TASKS",
						OnePersonTeamSource.class.getName());

				// Map request handler
				context.mapFlow("HANDLE_HTTP_REQUEST", sectionName,
						sectionInputName);
			}
		};
	}

	/**
	 * Convenience method to auto-wire in a
	 * {@link HttpsServerSocketManagedObjectSource} into an
	 * {@link AutoWireApplication}.
	 * 
	 * @param source
	 *            {@link AutoWireApplication}.
	 * @param port
	 *            Port to listen for HTTPS requests.
	 * @param sslEngineSourceClass
	 *            {@link SslEngineSource} class. May be <code>null</code>.
	 * @param sectionName
	 *            Name of the {@link AutoWireSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link SectionInput} on the
	 *            {@link AutoWireSection} servicing the requests.
	 * @return {@link AutoWireObject}.
	 */
	public static AutoWireObject autoWire(AutoWireApplication source, int port,
			Class<? extends SslEngineSource> sslEngineSourceClass,
			String sectionName, String sectionInputName) {

		// Create the wirer
		ManagedObjectSourceWirer wirer = createManagedObjectSourceWirer(
				sectionName, sectionInputName);

		// Add this managed object source
		AutoWireObject object = source.addManagedObject(
				HttpsServerSocketManagedObjectSource.class.getName(), wirer,
				new AutoWire(ServerHttpConnection.class));
		object.addProperty(PROPERTY_PORT, String.valueOf(port));
		if (sslEngineSourceClass != null) {
			object.addProperty(
					SslCommunicationProtocol.PROPERTY_SSL_ENGINE_SOURCE,
					sslEngineSourceClass.getName());
		}

		// Return the object
		return object;
	}

	/*
	 * ==================== ManagedObjectSourceService ====================
	 */

	@Override
	public String getManagedObjectSourceAlias() {
		return "HTTPS_SERVER";
	}

	@Override
	public Class<HttpsServerSocketManagedObjectSource> getManagedObjectSourceClass() {
		return HttpsServerSocketManagedObjectSource.class;
	}

	/*
	 * ============= AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocolSource createCommunicationProtocolSource() {
		return new SslCommunicationProtocol(new HttpCommunicationProtocol());
	}

}