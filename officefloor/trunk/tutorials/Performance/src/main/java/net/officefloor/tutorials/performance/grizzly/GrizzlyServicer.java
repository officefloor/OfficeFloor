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
package net.officefloor.tutorials.performance.grizzly;

import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.servlet.HttpServletServicer;

import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.servlet.WebappContext;

/**
 * Glassfish {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class GrizzlyServicer implements Servicer {

	/**
	 * Grizzly server.
	 */
	private HttpServer server;

	@Override
	public void start() throws Exception {
		this.server = new HttpServer();
		this.server.addListener(new NetworkListener("grizzly",
				NetworkListener.DEFAULT_NETWORK_HOST, new PortRange(7000)));
		WebappContext context = new WebappContext("Grizzly");
		context.addServlet("test", new HttpServletServicer());
		context.deploy(server);
		this.server.start();
	}

	@Override
	public void stop() throws Exception {
		this.server.stop();
	}

}