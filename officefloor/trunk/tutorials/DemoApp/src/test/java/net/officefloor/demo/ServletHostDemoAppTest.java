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
package net.officefloor.demo;

import java.io.File;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * Tests within a JEE {@link Servlet} container.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHostDemoAppTest extends OfficeFrameTestCase {

	/**
	 * <p>
	 * Finds the <code>webapp</code> directory.
	 * <p>
	 * It first attempts to look for a target directory containing the compiled
	 * GWT content and if not available falls back to the
	 * <code>src/main/webapp</code>.
	 * 
	 * @return {@link File} location of the <code>webapp</code> directory.
	 */
	public static File findWebApDirectory() {

		// Find target directory (as has incrementing version)
		File targetDir = new File(".", "target");
		File webAppDir = null;
		if (targetDir.exists()) {
			for (File dir : targetDir.listFiles()) {

				// Ignore if not directory (ignores packaged content)
				if (!(dir.isDirectory())) {
					continue;
				}

				// Determine if webap directory
				if (dir.getName().startsWith("DemoApp-")) {
					// Found the webapp directory
					webAppDir = dir;
				}
			}
		}

		// Determine if target webapp directory is suitable to use
		if ((webAppDir != null)
				&& (webAppDir.exists())
				&& (new File(webAppDir, WoofOfficeFloorSource.WEBXML_FILE_PATH)
						.exists())) {
			// Use target compiled directory
			return webAppDir;
		}

		// Use source webapp directory
		System.err.println("WARNING: using "
				+ WoofOfficeFloorSource.WEBAPP_PATH
				+ " so GWT functionality will NOT be available");
		return new File(".", WoofOfficeFloorSource.WEBAPP_PATH);
	}

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

	/**
	 * Runs the application.
	 */
	public void testRun() throws Exception {
		// Start the server
		this.server = startServer("", 7979, findWebApDirectory());
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the server
		this.server.stop();
	}

}