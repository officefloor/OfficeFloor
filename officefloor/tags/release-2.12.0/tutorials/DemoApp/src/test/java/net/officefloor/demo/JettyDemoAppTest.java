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
package net.officefloor.demo;

import java.io.File;

import javax.servlet.Servlet;

import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.servlet.WoofServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Tests within a JEE {@link Servlet} container.
 * 
 * @author Daniel Sagenschneider
 */
public class JettyDemoAppTest extends AbstractDemoAppTestCase {

	/**
	 * Starts the {@link Server}.
	 * 
	 * @param contextPath
	 *            Context path.
	 * @param port
	 *            Port.
	 * @param webAppDir
	 *            <code>webapp</code> directory.
	 * @return Started {@link Server}.
	 */
	private static Server startServer(String contextPath, int port,
			File webAppDir) throws Exception {

		// Configure Servlet container
		Server server = new Server(port);
		WebAppContext context = new WebAppContext();
		context.setContextPath("".equals(contextPath) ? "/" : contextPath);
		context.setResourceBase(webAppDir.getAbsolutePath());
		context.setSessionHandler(new SessionHandler());
		server.setHandler(context);

		// If running from clean, WoOF Servlet webfragment not available
		context.addEventListener(new WoofServlet());

		// Start the server
		server.start();

		// Return the server
		return server;
	}

	/**
	 * Runs the application for manual testing.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) throws Exception {

		// Start the server
		Server server = startServer("", 7979, findWebApDirectory());

		try {
			// Wait to stop
			System.out.print("Press enter to finish");
			System.out.flush();
			System.in.read();

		} finally {
			// Stop Server
			server.stop();
		}
	}

	/**
	 * {@link Server}.
	 */
	private Server server = null;

	@Override
	protected int startServer() throws Exception {

		// Obtain port for running
		int port = MockHttpServer.getAvailablePort();

		// Start the server
		this.server = startServer("", port, findWebApDirectory());

		// Return the port
		return port;
	}

	@Override
	protected void stopServer() throws Exception {
		// Stop the server
		this.server.stop();
	}

}