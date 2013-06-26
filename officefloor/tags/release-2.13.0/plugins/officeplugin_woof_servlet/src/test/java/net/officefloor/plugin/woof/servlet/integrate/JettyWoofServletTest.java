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
package net.officefloor.plugin.woof.servlet.integrate;

import java.io.File;

import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.servlet.WoofServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Tests the {@link WoofServlet} within Jetty.
 * 
 * @author Daniel Sagenschneider
 */
public class JettyWoofServletTest extends
		AbstractWoofServletTestCase {

	/**
	 * {@link Server}.
	 */
	private Server server;

	/*
	 * =================== Setup/Teardown/Helper ==========================
	 */

	@Override
	protected int startServer(String contextPath) throws Exception {

		// Obtain the port
		int port = MockHttpServer.getAvailablePort();

		// Find the base directory for resources
		File baseDirectory = new File(".", "src/test/webapp");
		assertTrue("Base directory should exist", baseDirectory.isDirectory());

		// Start servlet container with filter
		this.server = new Server(port);
		WebAppContext context = new WebAppContext();
		context.setContextPath("".equals(contextPath) ? "/" : contextPath);
		context.setResourceBase(baseDirectory.getAbsolutePath());
		context.setSessionHandler(new SessionHandler());

		// Add WoOF
		context.addEventListener(new WoofServlet());

		// Start the server
		this.server.setHandler(context);
		this.server.start();

		// Return the port
		return port;
	}

	@Override
	protected void stopServer() throws Exception {
		// Stop the server
		if (this.server != null) {
			this.server.stop();
		}
	}

}