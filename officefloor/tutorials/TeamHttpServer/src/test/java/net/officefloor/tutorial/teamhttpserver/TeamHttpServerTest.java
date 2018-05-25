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
package net.officefloor.tutorial.teamhttpserver;

import java.sql.Connection;
import java.sql.ResultSet;

import junit.framework.TestCase;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link TeamHttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamHttpServerTest extends TestCase {

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@Override
	protected void setUp() throws Exception {

		// Keep connection to database to keep in memory database alive
		this.connection = DefaultDataSourceFactory.createDataSource("datasource.properties").getConnection();

		// Start the database and HTTP Server
		this.server = MockWoofServer.open();

		// Request page to allow time for database setup
		this.server.send(MockHttpServer.mockRequest("/example"));
	}

	@Override
	protected void tearDown() throws Exception {

		// Stop the server
		if (this.server != null) {
			this.server.close();
		}

		// Close connection (to clean up database)
		if (this.connection != null) {
			this.connection.close();
		}
	}

	/**
	 * Ensure able to connect to database.
	 */
	public void testConnection() throws Exception {

		// Request page to allow time for database setup
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals("Should be sucessful", 200, response.getStatus().getStatusCode());

		// Ensure can get initial row
		ResultSet resultSet = this.connection.createStatement()
				.executeQuery("SELECT CODE FROM LETTER_CODE WHERE LETTER = 'A'");
		assertTrue("Ensure have result", resultSet.next());
		assertEquals("Incorrect code for letter", "Y", resultSet.getString("CODE"));
		assertFalse("Ensure no further results", resultSet.next());
	}

	/**
	 * Requests page from HTTP Server.
	 */
	// START SNIPPET: test
	public void testRetrieveEncryptions() throws Exception {

		// Retrieving from database (will have value cached)
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example+encrypt?letter=A"));
		assertEquals("Follow POST then GET pattern", 303, response.getStatus().getStatusCode());
		assertFalse("Ensure not cached (obtain from database)", response.getEntity(null).contains("[cached]"));

		// Looking up within cache
		response = this.server.send(MockHttpServer.mockRequest("/example+encrypt?letter=A"));
		assertEquals("Follow POST then GET pattern", 303, response.getStatus().getStatusCode());
		assertFalse("Ensure cached", response.getEntity(null).contains("[cached]"));
	}
	// END SNIPPET: test

}