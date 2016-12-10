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

import net.officefloor.tutorials.performance.logic.HttpServletServicer;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Jetty {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class JettyServicer implements Servicer {

	/**
	 * {@link Server}.
	 */
	private Server server;

	@Override
	public int getPort() {
		return 8080;
	}

	@Override
	public int getMaximumConnectionCount() {
		return 10000;
	}

	@Override
	public void start() throws Exception {

		// Create the server
		this.server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8080);
		connector.setAcceptQueueSize(25000);
		this.server.setConnectors(new Connector[] { connector });
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		this.server.setHandler(context);
		context.addServlet(new ServletHolder(new HttpServletServicer()), "/*");
		this.server.start();
	}

	@Override
	public void stop() throws Exception {
		this.server.stop();
	}

}