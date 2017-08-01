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
package net.officefloor.server.http.source;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.protocol.CommunicationProtocolSource;
import net.officefloor.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSource extends AbstractServerSocketManagedObjectSource {

	/**
	 * Convenience method to configure the
	 * {@link HttpServerSocketManagedObjectSource}.
	 * 
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @param port
	 *            Port to listen for HTTP requests.
	 * @param office
	 *            {@link DeployedOffice}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection} servicing the requests.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput} on the
	 *            {@link OfficeSection} servicing the requests.
	 * @return {@link OfficeFloorInputManagedObject}.
	 */
	public static OfficeFloorInputManagedObject configure(OfficeFloorDeployer deployer, int port, DeployedOffice office,
			String sectionName, String sectionInputName) {

		// Add this managed object source
		OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("HTTP_SOURCE_" + port,
				HttpServerSocketManagedObjectSource.class.getName());
		mos.addProperty(PROPERTY_PORT, String.valueOf(port));

		// Managed by office
		deployer.link(mos.getManagingOffice(), office);

		// Add teams for the managed object source
		deployer.link(mos.getManagedObjectTeam("listener"),
				deployer.addTeam("LISTENER", ExecutorCachedTeamSource.class.getName()));

		// Handle servicing of requests
		deployer.link(mos.getManagedObjectFlow("HANDLE_HTTP_REQUEST"),
				office.getDeployedOfficeInput(sectionName, sectionInputName));

		// Create the input managed object
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP_" + port);
		input.addTypeQualification(null, ServerHttpConnection.class.getName());
		deployer.link(mos, input);

		// Return the input managed object
		return input;
	}

	/*
	 * ============= AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected CommunicationProtocolSource createCommunicationProtocolSource() {
		return new HttpCommunicationProtocol();
	}

}