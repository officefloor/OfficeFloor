/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.tutorial.resthttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.EntityManagerRule;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockObjectResponse;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the REST end points.
 * 
 * @author Daniel Sagenschneider
 */
public class RestHttpServerTest {

	// START SNIPPET: calling
	@ClassRule
	public static DataSourceRule dataSource = new DataSourceRule("datasource.properties");

	@Rule
	public EntityManagerRule entityManager = new EntityManagerRule("entitymanager.properties",
			new HibernateJpaManagedObjectSource(), dataSource);

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	@Before
	public void cleanDatabase() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			connection.createStatement().executeUpdate("TRUNCATE TABLE VEHICLE");
		}
	}

	@Test
	public void postMissingData() throws Exception {

		// POST with missing data
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST).entity("{}"));
		response.assertResponse(444, "{\"error\":\"Must have vehicleType\"}");
	}

	@Test
	public void postEntry() throws Exception {

		// POST to create row and validate successful
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle").method(HttpMethod.POST)
				.header("content-type", "application/json").entity("{ \"vehicleType\": \"bike\", \"wheels\": 2 }"));
		response.assertResponse(204, "");

		// Ensure row created
		Vehicle vehicle = this.entityManager.getEntityManager()
				.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'bike'", Vehicle.class).getSingleResult();
		assertNotNull("Should have row created", vehicle);
		assertEquals("Incorrect row", 2, vehicle.getWheels().intValue());
	}

	@Test
	public void getEntry() throws Exception {

		// Create entry
		Vehicle vehicle = new Vehicle("car", 4);
		this.entityManager.getEntityManager().persist(vehicle);
		this.entityManager.getEntityManager().getTransaction().commit();

		// GET entry
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/vehicle/" + vehicle.getId()));
		response.assertResponse(200, "{\"vehicleType\":\"car\",\"wheels\":4,\"id\":" + vehicle.getId() + "}",
				"content-type", "application/json");
	}
	// END SNIPPET: calling

	// START SNIPPET: pojo
	@Test
	public void createWithMissingData() {
		try {
			new RestLogic().createVehicle(new Vehicle(), this.entityManager.getEntityManager());
			fail("Should not be successful");
		} catch (HttpException ex) {
			assertEquals("Incorrect status", 444, ex.getHttpStatus().getStatusCode());
			assertEquals("Incorrect reason", "Must have vehicleType", ex.getMessage());
		}
	}

	@Test
	public void createVehicle() {

		// Create the vehicle
		new RestLogic().createVehicle(new Vehicle("tricycle", 3), this.entityManager.getEntityManager());

		// Ensure row created
		Vehicle vehicle = this.entityManager.getEntityManager()
				.createQuery("SELECT V FROM Vehicle V WHERE vehicleType = 'tricycle'", Vehicle.class).getSingleResult();
		assertNotNull("Should have row created", vehicle);
		assertEquals("Incorrect row", 3, vehicle.getWheels().intValue());
	}

	@Test
	public void getVehicle() {

		// Create entry
		Vehicle created = new Vehicle("unicycle", 1);
		this.entityManager.getEntityManager().persist(created);

		// Obtain the vehicle
		MockObjectResponse<Vehicle> response = new MockObjectResponse<>();
		new RestLogic().getVehicle(String.valueOf(created.getId()), this.entityManager.getEntityManager(), response);
		Vehicle vehicle = response.getObject();
		assertEquals("Incorrect row sent", "unicycle", vehicle.getVehicleType());
	}
	// END SNIPPET: pojo

}