/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.socket.server.http.HttpServer.HttpServerFlows;

/**
 * Provides the startup of the {@link HttpServer} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class HttpServerStartup extends AbstractOfficeConstructTestCase {

	/**
	 * Starting port number.
	 */
	private static int portStart = 12643;

	/**
	 * Port number to use for testing.
	 */
	private int port;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		// Specify the port
		port = portStart;
		portStart++; // increment for next test

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder<HttpServerFlows> serverSocketBuilder = this
				.constructManagedObject("MO",
						HttpServerSocketManagedObjectSource.class);
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_PORT, String
						.valueOf(port));
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
				"1024");
		serverSocketBuilder.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_MESSAGE_SIZE, "3");
		serverSocketBuilder.setDefaultTimeout(3000);

		// Have server socket managed by office
		ManagingOfficeBuilder<HttpServerFlows> managingOfficeBuilder = serverSocketBuilder
				.setManagingOffice(officeName);
		managingOfficeBuilder.setProcessBoundManagedObjectName("MO");

		// Register the necessary teams for socket listening
		this.constructTeam("ACCEPTER_TEAM", new OnePersonTeam(100));
		officeBuilder.registerTeam("of-MO.accepter", "of-ACCEPTER_TEAM");
		this.constructTeam("LISTENER_TEAM", new WorkerPerTaskTeam("Listener"));
		officeBuilder.registerTeam("of-MO.listener", "of-LISTENER_TEAM");
		this.constructTeam("CLEANUP_TEAM", new PassiveTeam());

		// Register the task to service the HTTP request
		final TaskReference task = this.registerHttpServiceTask();
		managingOfficeBuilder.linkProcess(HttpServerFlows.HANDLE_HTTP_REQUEST,
				task.workName, task.taskName);

		// Create and open the Office Floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();
	}

	/**
	 * Obtains the URL of the server.
	 * 
	 * @return URL of the server.
	 */
	public String getServerUrl() {
		return "http://localhost:" + this.port;
	}

	/**
	 * Registers the {@link Task}(s) to service the HTTP request and returns
	 * reference to the entry {@link Task}.
	 * 
	 * @return {@link TaskReference} to the {@link Task} to handle the HTTP
	 *         request.
	 */
	protected abstract TaskReference registerHttpServiceTask() throws Exception;

	/**
	 * Reference to a {@link Task}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	protected class TaskReference {

		/**
		 * Name of the {@link Work}.
		 */
		public final String workName;

		/**
		 * Name of the {@link Task}.
		 */
		public final String taskName;

		/**
		 * Initiate.
		 * 
		 * @param workName
		 *            Name of the {@link Work}.
		 * @param taskName
		 *            Name of the {@link Task}.
		 */
		public TaskReference(String workName, String taskName) {
			this.workName = workName;
			this.taskName = taskName;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.test.AbstractOfficeConstructTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {

		// Close the office
		this.officeFloor.closeOfficeFloor();

		// Clean up
		super.tearDown();
	}

}