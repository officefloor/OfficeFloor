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
package net.officefloor.tutorial.transactionhttpserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.sql.DataSource;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hsqldb.jdbc.jdbcDataSource;

/**
 * Tests the Transaction HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class TransactionHttpServerTest extends TestCase {

	/**
	 * URL for the database.
	 */
	private static final String DATABASE_URL = "jdbc:hsqldb:mem:exampleDb";

	/**
	 * User for the database.
	 */
	private static final String DATABASE_USER = "sa";

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	@Override
	protected void setUp() throws Exception {
		// Start the database and HTTP Server
		WoofOfficeFloorSource.start();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Disconnect client
			this.client.close();
		} finally {
			try {
				// Stop HTTP Server
				WoofOfficeFloorSource.stop();
			} finally {
				// Stop database for new instance each test
				DriverManager.getConnection(DATABASE_URL, DATABASE_USER, "")
						.createStatement().execute("SHUTDOWN IMMEDIATELY");
			}
		}
	}

	/**
	 * Ensure able to connect to database with {@link DataSource}.
	 */
	public void testSetupDatabase() throws Exception {

		// Request page to allow time for database setup
		this.doRequest("http://localhost:7878/users.woof");

		// Obtain connection via DataSource
		jdbcDataSource dataSource = new jdbcDataSource();
		dataSource.setDatabase(DATABASE_URL);
		dataSource.setUser(DATABASE_USER);
		Connection connection = dataSource.getConnection();

		// Ensure can get initial row
		ResultSet resultSet = connection.createStatement().executeQuery(
				"SELECT FULLNAME FROM PERSON");
		assertTrue("Ensure have result", resultSet.next());
		assertEquals("Incorrect setup name", "Daniel Sagenschneider",
				resultSet.getString("FULLNAME"));
		assertFalse("Ensure no further results", resultSet.next());
		resultSet.close();
	}

	/**
	 * Ensure the JPA connects to database.
	 */
	public void testJpa() throws Exception {

		// Request page to allow time for database setup
		this.doRequest("http://localhost:7878/users.woof");

		// Obtain entity manager
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("example");
		EntityManager manager = factory.createEntityManager();

		// Ensure can obtain user and person
		Query query = manager.createQuery("SELECT U FROM User U");
		User user = (User) query.getSingleResult();
		assertEquals("Incorrect user name", "daniel", user.getUserName());
		Person person = user.getPerson();
		assertEquals("Incorrect person name", "Daniel Sagenschneider",
				person.getFullName());

		// Ensure persist user and person
		User newUser = new User();
		newUser.setUserName("test");
		Person newPerson = new Person();
		newPerson.setFullName("TEST");
		newPerson.setUser(newUser);
		manager.persist(newPerson);
		manager.close();

		// Ensure user and person persisted
		manager = factory.createEntityManager();
		User retrievedUser = manager.find(User.class, newUser.getId());
		assertEquals("Incorrect retrieved user name", "test",
				retrievedUser.getUserName());
		Person retrievedPerson = retrievedUser.getPerson();
		assertEquals("Incorrect retrieved full name", "TEST",
				retrievedPerson.getFullName());

		// Close persistence
		factory.close();
	}

	/**
	 * Requests the page, creates a user and fails to create a user.
	 */
	// START SNIPPET: test
	public void testCreateUser() throws Exception {

		// Request page
		this.doRequest("http://localhost:7878/users.woof");

		// Create user with all details
		this.doRequest("http://localhost:7878/users-create.woof?username=melanie&fullname=Melanie+Sagenschneider");

		// Attempt to create user that will fail database constraints
		this.doRequest("http://localhost:7878/users-create.woof?username=joe");

		// Validate melanie added
		EntityManager manager = Persistence.createEntityManagerFactory(
				"example").createEntityManager();
		User melanie = (User) manager.createQuery(
				"SELECT U FROM User U WHERE U.userName = 'melanie'")
				.getSingleResult();
		assertEquals("Melanie created", "Melanie Sagenschneider", melanie
				.getPerson().getFullName());

		// Validate joe not added
		try {
			manager.createQuery("SELECT U FROM User U WHERE U.userName = 'joe'")
					.getSingleResult();
			fail("Should not find Joe");
		} catch (NoResultException ex) {
		}
	}

	private void doRequest(String url) throws Exception {
		HttpResponse response = this.client.execute(new HttpGet(url));
		assertEquals("Request should be successful", 200, response
				.getStatusLine().getStatusCode());
		response.getEntity().writeTo(System.out);
	}
	// END SNIPPET: test

}