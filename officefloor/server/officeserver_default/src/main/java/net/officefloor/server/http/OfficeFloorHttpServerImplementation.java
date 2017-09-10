/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServerImplementation implements HttpServerImplementation {

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {

		// Obtain the deployer
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();

		// Configure the input
		OfficeFloorInputManagedObject input = deployer.addInputManagedObject("HTTP",
				ServerHttpConnection.class.getName());

		// Obtain the handling
		DeployedOfficeInput serviceInput = context.getInternalServiceInput();
		DeployedOffice office = serviceInput.getDeployedOffice();

		// Configure the non-secure HTTP
		OfficeFloorManagedObjectSource http = deployer.addManagedObjectSource("HTTP",
				new HttpServerSocketManagedObjectSource(context.getHttpPort()));
		deployer.link(http.getManagingOffice(), office);
		deployer.link(http.getManagedObjectFlow(HttpServerSocketManagedObjectSource.Flows.HANDLE_REQUEST.name()),
				serviceInput);
		deployer.link(http, input);

		// Configure the secure HTTP
		int httpsPort = context.getHttpsPort();
		if (httpsPort > 0) {
			OfficeFloorManagedObjectSource https = deployer.addManagedObjectSource("HTTPS",
					new HttpServerSocketManagedObjectSource(httpsPort, context.getSslContext()));
			deployer.link(https.getManagingOffice(), office);
			deployer.link(https.getManagedObjectFlow(HttpServerSocketManagedObjectSource.Flows.HANDLE_REQUEST.name()),
					serviceInput);
			deployer.link(https, input);

			// Specify default bound name
			input.setBoundOfficeFloorManagedObjectSource(http);
		}
	}

}
