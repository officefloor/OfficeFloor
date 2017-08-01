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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.source.HttpsServerSocketManagedObjectSource;

/**
 * {@link OfficeFloor} {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServerImplementation implements HttpServerImplementation {

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {

		// Configure HTTP and HTTPS
		HttpsServerSocketManagedObjectSource.configure(context.getOfficeFloorDeployer(), context.getHttpPort(),
				context.getHttpsPort(), context.getSslContext(), context.getInternalServiceHandler());

	}

}
