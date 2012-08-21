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
package net.officefloor.tutorials.performance;

import java.util.concurrent.Executors;

import net.officefloor.tutorials.performance.logic.HttpServletServicer;

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
	public int getPort() {
		return 7000;
	}

	@Override
	public int getMaximumConnectionCount() {
		return 10000;
	}

	@Override
	public void start() throws Exception {
		this.server = new HttpServer();
		NetworkListener listener = new NetworkListener("grizzly",
				NetworkListener.DEFAULT_NETWORK_HOST, new PortRange(
						this.getPort()));
		listener.getTransport().setWorkerThreadPool(
				Executors.newCachedThreadPool());
		listener.getTransport().setServerConnectionBackLog(25000);
		this.server.addListener(listener);
		WebappContext context = new WebappContext("Grizzly");
		context.addServlet("test", new HttpServletServicer()).addMapping("/*");
		context.deploy(this.server);
		this.server.start();
	}

	@Override
	public void stop() throws Exception {
		this.server.stop();
	}

}